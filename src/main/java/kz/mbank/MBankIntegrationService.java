package kz.mbank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.mbank.client.dto.HasTimeOutFlag;
import kz.mbank.client.dto.PaymentRequestDto;
import kz.mbank.client.dto.PaymentResponse;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import ru.crystals.pos.api.events.ShiftEventListener;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;
import ru.crystals.pos.spi.plugin.payment.InvalidPaymentException;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import setapi.plugin.lib.config.PluginConfigProperties;
import setapi.plugin.lib.config.UIConfig;
import setapi.plugin.lib.exception.BaseError;
import setapi.plugin.lib.exception.TerminalUnavailableError;
import setapi.plugin.lib.service.BankIntegrationService;
import setapi.plugin.lib.service.RequestBuildingService;
import setapi.plugin.lib.service.UIService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;

import static setapi.plugin.lib.config.HttpClientConfig.getBuilder;
import static setapi.plugin.lib.config.LogConfig.getLogger;
import static setapi.plugin.lib.config.ObjectMapperConfig.getInstance;
import static setapi.plugin.lib.config.PluginConfig.getProperties;
import static setapi.plugin.lib.config.RequestBuilderConfig.getRequestBuilder;
import static setapi.plugin.lib.service.TransactionStartHandler.checkTransactionExpired;

public class MBankIntegrationService implements BankIntegrationService, ShiftEventListener {

    public static final String BANK_ID = "MBANK";
    protected final PluginConfigProperties configProperties = getProperties();
    private final Logger logger = getLogger();
    private final RequestBuildingService requestBuilder = getRequestBuilder();
    private final ObjectMapper mapper = getInstance();
    private final UIService ui = UIConfig.getUiService();

    @Override
    public void onShiftOpened(int newShiftNumber) {
        executeWithHandling("api/shifts", null);
    }

    @Override
    public void onShiftClosed() {
        executeWithHandling("api/shifts/inactive", null);
    }

    @Override
    public void process(PaymentRequest paymentRequest, int amount) {
        val requestDto = new PaymentRequestDto();
        requestDto.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
        PaymentResponse response = executeWithHandling("payment", requestDto);
        if (!response.isSuccess()) {
            throw new BaseError(response.getMessage());
        }
        try {
            Payment payment = new Payment();
            payment.setSum(BigDecimal.valueOf(amount));
            paymentRequest.getPaymentCallback().paymentCompleted(payment);
        } catch (InvalidPaymentException e) {
            throw new BaseError(e);
        }
    }

    @Override
    public void process(RefundRequest refundRequest, int amount) {
        logger.error("Refund is not implemented");
    }

    @Override
    public void process(CancelRequest cancelRequest, int amount) {
        logger.error("Callback is not implemented");
    }

    private <REQ extends HasTimeOutFlag, RES> RES executeWithHandling(String url, REQ request) {

        val post = requestBuilder.buildPost(url, request);

        ui.showSpinner("connecting");

        InputStream responseContent;
        try (CloseableHttpClient client = getBuilder().build()) {
            responseContent = client.execute(post).getEntity().getContent();
        } catch (SocketTimeoutException e) {
            if (checkTransactionExpired()) {
                throw new TerminalUnavailableError(e);
            }
            return refresh(url, request);
        } catch (IOException e) {
            throw new TerminalUnavailableError(e);
        }

        RES response;
        try {
            response = mapper.readValue(responseContent, new TypeReference<RES>() {
            });
        } catch (IOException e) {
            throw new BaseError(e);
        }

        return response;
    }

    private <REQ extends HasTimeOutFlag, RES> RES refresh(String url, REQ request) {
        request.setIsTimeOut(true);
        return executeWithHandling(url, request);
    }
}
