package kz.kaspi.qr.plugin.integration;

import common.config.LogConfig;
import common.config.UIConfig;
import common.exception.BaseError;
import common.service.UIService;
import kz.kaspi.qr.plugin.integration.config.ClientConfig;
import kz.kaspi.qr.plugin.integration.dto.Create;
import kz.kaspi.qr.plugin.integration.dto.DeviceRegistration;
import kz.kaspi.qr.plugin.integration.dto.DeviceToken;
import kz.kaspi.qr.plugin.integration.dto.Payment;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatusData;
import kz.kaspi.qr.plugin.integration.dto.Return;
import kz.kaspi.qr.plugin.integration.dto.StatusCode;
import kz.kaspi.qr.plugin.integration.dto.TradePoint;
import kz.kaspi.qr.plugin.integration.dto.response.KaspiQRPayResponse;
import lombok.val;
import org.slf4j.Logger;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;

public class KaspiQRPayService {

    private static final long FIXED_DELAY = 5000;
    private final KaspiQRPayClient client;
    private final KaspiQRPayClient clientV2;
    private final ThreadLocal<Long> start = new ThreadLocal<>();
    private final ThreadLocal<Long> lastPollCall = new ThreadLocal<>();
    private final ThreadLocal<OffsetDateTime> expiration = new ThreadLocal<>();
    private final ThreadLocal<Long> interval = new ThreadLocal<>();
    private final ThreadLocal<Long> waitTimeout = new ThreadLocal<>();
    private final ThreadLocal<Long> confirmTimeout = new ThreadLocal<>();
    private final ThreadLocal<Long> returnLastPollCall = new ThreadLocal<>();
    private final ThreadLocal<OffsetDateTime> returnExpiration = new ThreadLocal<>();
    private final Logger logger = LogConfig.getLogger();
    private final UIService uiService = UIConfig.getUiService();

    public KaspiQRPayService() {
        val retrofit = ClientConfig.getInstance().getRetrofit();
        val retrofitV2 = ClientConfig.getInstanceV2().getRetrofit();
        client = retrofit.create(KaspiQRPayClient.class);
        clientV2 = retrofitV2.create(KaspiQRPayClient.class);
    }

    public Collection<TradePoint> getTradePoints() {
        return executeWithHandling(client.getTradePoints());
    }

    public String registerDevice(String tradePointId, String deviceId) {
        val deviceRegistration = new DeviceRegistration(tradePointId, deviceId);

        KaspiQRPayResponse<DeviceToken> response;
        try {
            response = executeWithBaseHandling(client.register(deviceRegistration));
        } catch (IOException e) {
            logger.error("Failed on request execution", e);
            throw new BaseError(e);
        }

        val sc = Optional.ofNullable(response)
                .map(KaspiQRPayResponse::getStatusCode)
                .orElse(StatusCode.UNKNOWN);

        if (!StatusCode.SUCCESS.equals(sc)) {
            uiService.showError("Статус при регистрации устройства " + sc, () -> {
            });
            return null;
        }


        val deviceToken = Optional.ofNullable(response)
                .map(KaspiQRPayResponse::getData)
                .map(DeviceToken::getDeviceToken)
                .orElse(null);


        if (Objects.isNull(deviceToken)) {
            uiService.showError("Не получен токен устройства" + sc, () -> {
            });
            return null;
        }

        return deviceToken;
    }

    public void deleteDevice(String token) {
        val deviceToken = new DeviceToken(token);
        executeWithHandling(client.delete(deviceToken));
    }

    public PaymentDetails getDetails(String paymentId, String token) {
        return executeWithHandling(client.getDetails(paymentId, token));
    }

    public Payment paymentCreate(String token, BigDecimal amount, String id) {
        val qrCreate = new Create(token, id, amount);
        val qrPayment = executeWithHandling(client.paymentCreate(qrCreate));

        long now = currentTimeMillis();
        start.set(now);
        lastPollCall.set(now);
        expiration.set(qrPayment.getExpireDate());
        val behaviourOptions = qrPayment.getQrPaymentBehaviorOptions();
        interval.set(behaviourOptions.getStatusPollingInterval());
        waitTimeout.set(behaviourOptions.getQrCodeScanWaitTimeout());
        confirmTimeout.set(behaviourOptions.getPaymentConfirmationTimeout());

        return qrPayment;
    }

    public Return returnCreate(String token, BigDecimal amount, String id) {
        val create = new Create(token, id, amount);
        val qrReturn = executeWithHandling(client.returnCreate(create));

        long now = currentTimeMillis();
        start.set(now);
        returnLastPollCall.set(now);
        returnExpiration.set(qrReturn.getExpireDate());
        val behaviourOptions = qrReturn.getQrReturnBehaviorOptions();
        interval.set(behaviourOptions.getQrCodeScanEventPollingInterval());
        confirmTimeout.set(180L);
        waitTimeout.set(behaviourOptions.getQrCodeScanWaitTimeout());

        return qrReturn;
    }

    public void updateStatus(Context context) throws IOException {
        waitInterval();
        val call = Objects.requireNonNull(context.getType()) == Context.Type.RETURN
                ? client.getReturnStatus(context.getId())
                : clientV2.getPaymentStatus(context.getId());

        val response = executeWithBaseHandling(call);

        Optional.ofNullable(response.getData())
                .ifPresent(context::setStatusData);

        if (!StatusCode.SUCCESS.equals(Objects.requireNonNull(response).getStatusCode())) {
            context.getStatusData().setStatus(PaymentStatus.ERROR);
            context.setMessage(response.getMessage());
        }

    }

    private boolean isNonExpired(PaymentStatus paymentStatus) {
        val now = currentTimeMillis();
        long fromStart = now - start.get() - FIXED_DELAY;

        logger.debug("Время " + fromStart / 1000);

        switch (Optional.ofNullable(paymentStatus).orElse(PaymentStatus.UNKNOWN)) {
            case CREATED:
                return fromStart < waitTimeout.get() * 1000;
            case WAIT:
                return fromStart < confirmTimeout.get() * 1000;
            default:
                return fromStart < 180000;
        }
    }

    public boolean isNonExpired(Context context) {
        return isNonExpired(Optional.ofNullable(context)
                .map(Context::getStatusData)
                .map(PaymentStatusData::getStatus)
                .orElse(PaymentStatus.UNKNOWN));
    }

    private void waitInterval() {
        long actualInterval = currentTimeMillis() - lastPollCall.get();

        val intervalMillis = interval.get() * 1000;

        if (actualInterval < intervalMillis) {
            try {
                Thread.sleep(intervalMillis - actualInterval);
            } catch (InterruptedException e) {
                throw new BaseError(e);
            }
        }
    }

    private <T> T executeWithHandling(Call<? extends KaspiQRPayResponse<T>> call) {
        try {
            return executeWithBaseHandling(call).getData();
        } catch (IOException e) {
            logger.error("Failed on request execution", e);
            throw new BaseError(e);
        }
    }

    private <T> KaspiQRPayResponse<T> executeWithBaseHandling(Call<? extends KaspiQRPayResponse<T>> call) throws IOException {
        lastPollCall.set(System.currentTimeMillis());
        Response<? extends KaspiQRPayResponse<T>> response = call.execute();

        KaspiQRPayResponse<T> body = response.body();
        if (!response.isSuccessful()) {
            logger.error("Failed on request execution. Code: {}, body {}", response.code(), body);
            throw new BaseError(response.message());
        }

        return body;
    }

}
