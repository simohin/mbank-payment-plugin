package kz.kaspi.qr.plugin.integration;

import common.config.LogConfig;
import common.exception.BaseError;
import kz.kaspi.qr.plugin.integration.config.ClientConfig;
import kz.kaspi.qr.plugin.integration.dto.Create;
import kz.kaspi.qr.plugin.integration.dto.DeviceRegistration;
import kz.kaspi.qr.plugin.integration.dto.DeviceToken;
import kz.kaspi.qr.plugin.integration.dto.Payment;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
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

    private final KaspiQRPayClient client;
    private final ThreadLocal<Long> start = new ThreadLocal<>();
    private final ThreadLocal<Long> lastPollCall = new ThreadLocal<>();
    private final ThreadLocal<OffsetDateTime> expiration = new ThreadLocal<>();
    private final ThreadLocal<Long> interval = new ThreadLocal<>();
    private final ThreadLocal<Long> waitTimeout = new ThreadLocal<>();
    private final ThreadLocal<Long> confirmTimeout = new ThreadLocal<>();
    private final ThreadLocal<Long> returnLastPollCall = new ThreadLocal<>();
    private final ThreadLocal<OffsetDateTime> returnExpiration = new ThreadLocal<>();
    private final ThreadLocal<Long> returnWaitTimeout = new ThreadLocal<>();
    private final ThreadLocal<Long> returnConfirmTimeout = new ThreadLocal<>();
    private final Logger logger = LogConfig.getLogger();

    public KaspiQRPayService() {
        val retrofit = ClientConfig.getInstance().getRetrofit();
        client = retrofit.create(KaspiQRPayClient.class);
    }

    public Collection<TradePoint> getTradePoints() {
        return executeWithHandling(client.getTradePoints());
    }

    public String registerDevice(String tradePointId, String deviceId) {
        val deviceRegistration = new DeviceRegistration(tradePointId, deviceId);
        return executeWithHandling(client.register(deviceRegistration)).getDeviceToken();
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

    public String returnCreate(String token, BigDecimal amount, String id) {
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

        return qrReturn.getQrReturnId();
    }

    public PaymentStatus getStatus(Context context) throws IOException {
        waitInterval();
        return executeWithBaseHandling(client.getPaymentStatus(context.getId())).getStatus();
    }

    private boolean isNonExpired(PaymentStatus paymentStatus) {
        val now = currentTimeMillis();
        long fromStart = now - start.get();

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
        return isNonExpired(context.getStatus());
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
            return executeWithBaseHandling(call);
        } catch (IOException e) {
            logger.error("Failed on request execution", e);
            throw new BaseError(e);
        }
    }

    private <T> T executeWithBaseHandling(Call<? extends KaspiQRPayResponse<T>> call) throws IOException {
        lastPollCall.set(System.currentTimeMillis());
        Response<? extends KaspiQRPayResponse<T>> response = call.execute();

        if (!response.isSuccessful()) {
            logger.error("Failed on request execution. Code: {}, body {}", response.code(), response.body());
            throw new BaseError(response.message());
        }

        if (!StatusCode.SUCCESS.equals(Objects.requireNonNull(response.body()).getStatusCode())) {
            throw new BaseError(response.body().getMessage());
        }

        return response.body().getData();
    }

}
