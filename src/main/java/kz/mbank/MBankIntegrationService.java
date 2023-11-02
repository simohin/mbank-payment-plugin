package kz.mbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.config.PluginConfigProperties;
import common.config.UIConfig;
import common.exception.BaseError;
import common.exception.TerminalUnavailableError;
import common.service.BankIntegrationService;
import common.service.RequestBuildingService;
import common.service.UIService;
import kz.mbank.client.dto.HasTimeOutFlag;
import kz.mbank.client.dto.PaymentRequestDto;
import kz.mbank.client.dto.PaymentResponse;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import ru.crystals.pos.api.events.ShiftEventListener;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.spi.plugin.payment.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.time.format.DateTimeFormatter;

import static common.config.HttpClientConfig.getBuilder;
import static common.config.LogConfig.getLogger;
import static common.config.ObjectMapperConfig.getInstance;
import static common.config.PluginConfig.getProperties;
import static common.config.RequestBuilderConfig.getRequestBuilder;
import static common.service.TransactionStartHandler.checkTransactionExpired;

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
        val receipt = paymentRequest.getReceipt();
        requestDto.setChequeNumber(receipt.getNumber());
        requestDto.setShiftNumber(receipt.getShiftNo());
        requestDto.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
        PaymentResponse response = executeWithHandling("payment", requestDto);
        if (!response.isSuccess()) {
            throw new BaseError(response.getMessage());
        }
        processSuccess(paymentRequest.getPaymentCallback(), requestDto, amount);
    }

    private void processSuccess(PaymentCallback callback, PaymentRequestDto requestDto, int amount) {

        val payment = new Payment();
        payment.setSum(BigDecimal.valueOf(amount));

        val data = payment.getData();
        String receipt;
        try {
            receipt = mapper.writeValueAsString(requestDto);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialise request dto", e);
            receipt = requestDto.toString();
        }
        data.put("mbank.payment.transaction.date", requestDto.getChequeDate().format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("mbank.payment.bank.id", BANK_ID);
        data.put("mbank.payment.payment.method", requestDto.getPaymentType().name().toLowerCase());
        data.put("mbank.payment.receipt.info", receipt);

        try {
            callback.paymentCompleted(payment);
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
