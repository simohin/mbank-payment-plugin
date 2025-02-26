package kz.kaspi.qr.plugin;

import common.config.PluginConfig;
import common.config.PluginConfigProperties;
import common.config.PrinterConfig;
import common.exception.BaseError;
import common.service.BankIntegrationService;
import common.service.UIService;
import common.service.slip.SlipProperties;
import kz.kaspi.qr.plugin.integration.Context;
import kz.kaspi.qr.plugin.integration.KaspiQRPayService;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatusData;
import kz.kaspi.qr.plugin.integration.dto.ReturnData;
import lombok.val;
import org.slf4j.Logger;
import ru.crystals.pos.api.comm.CommunicationMessage;
import ru.crystals.pos.api.events.ShiftEventListener;
import ru.crystals.pos.api.ext.loyal.dto.Slip;
import ru.crystals.pos.api.ext.loyal.dto.SlipParagraph;
import ru.crystals.pos.api.ext.loyal.dto.SlipParagraphType;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.spi.equipment.CustomerDisplay;
import ru.crystals.pos.spi.equipment.CustomerDisplayMessage;
import ru.crystals.pos.spi.equipment.SetApiPrinter;
import ru.crystals.pos.spi.equipment.SetApiPrinterException;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;
import ru.crystals.pos.spi.plugin.payment.InvalidPaymentException;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static common.config.CustomerDisplayConfig.getDisplay;
import static common.config.LogConfig.getLogger;
import static common.config.UIConfig.getUiService;
import static common.exception.BaseError.DEFAULT_ERROR_MESSAGE;
import static common.service.TransactionAmountHandler.getTransactionAmount;
import static common.service.slip.SlipService.buildSlip;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class KaspiQrIntegrationService implements BankIntegrationService, ShiftEventListener {

    private static final String KASPI_QR_BANK_ID = "KASPIQR";
    private static final String PAYMENT_ID = "kaspi.qr.payment.id";
    private static final String AMOUNT = "kaspi.qr.payment.amount";
    private static final String TERMINAL_NUMBER = "kaspi.qr.terminal.number";
    private static final String TRANSACTION_DATE = "kaspi.qr.transaction.date";
    private static final String BANK_ID = "kaspi.qr.bank.id";
    private static final String PAYMENT_METHOD = "kaspi.qr.payment.method";
    private static final String ADDRESS = "kaspi.qr.payment.address";
    private static final String CITY = "kaspi.qr.payment.city";
    private static final String STORE_NAME = "kaspi.qr.payment.store.name";
    private static final String LOAN_OFFER_NAME = "kaspi.qr.payment.loan.offer.name";
    private static final String LOAN_TERM = "kaspi.qr.payment.loan.term";
    private static final String IS_OFFER = "kaspi.qr.payment.is.offer";
    private static final String PRODUCT_TYPE = "kaspi.qr.payment.product.type";
    private static final String METHOD = "Kaspi QR";
    private static final String SCAN_QR_TITLE = "Сканируйте и платите Kaspi QR";
    private static final String SCAN_QR_TITLE_KZ = "Сканерлеңіз де, төлеңіз Kaspi QR";
    private static final String WAIT_CONFIRM_MESSAGE = "Төлемдi қосымшада растаңыз\nПодтвердите оплату в приложении";
    private static final String INIT_MESSAGE_RU = "Готовимся к приему оплаты";
    private static final String INIT_MESSAGE_KZ = "Төлем қабылдауға дайындалудамыз...";
    private static final String INIT_MESSAGE = INIT_MESSAGE_KZ + "\n" + INIT_MESSAGE_RU;
    private static final String SUCCESS_MESSAGE_RU = "Оплата принята";
    private static final String SUCCESS_MESSAGE_KZ = "Төлем қабылданды";
    private final Logger logger = getLogger();
    private final KaspiQRPayService service = new KaspiQRPayService();
    private final SetApiPrinter printer = PrinterConfig.getPrinter();
    private final PluginConfigProperties properties = PluginConfig.getProperties();
    private final String token;
    private final String terminalId;
    private final UIService uiService = getUiService();
    private final CustomerDisplay display = getDisplay();

    public KaspiQrIntegrationService() {
        val properties = PluginConfig.getProperties();

        String tradePointId = properties.getTradePointId();
        String deviceId = properties.getDeviceId();
        this.token = service.registerDevice(tradePointId, deviceId);
        this.terminalId = tradePointId + deviceId;
    }

    @Override
    public void onShiftOpened(int newShiftNumber) {
    }

    @Override
    public void onShiftClosed() {

    }

    @Override
    public void process(PaymentRequest paymentRequest, int amount) {
        showInitMessage();
        val scaledAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        val payment = service.paymentCreate(token, scaledAmount, UUID.randomUUID().toString());
        showQr(payment.getQrToken(), scaledAmount);
        val context = new Context(payment.getQrPaymentId(), paymentRequest.getPaymentCallback());
        process(context);
    }

    @Override
    public void process(RefundRequest refundRequest, int amount) {
        logger.trace("Start processing refund request {}", refundRequest);
        val scaledAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        val paymentId = refundRequest.getOriginalPayment().getData().get(PAYMENT_ID);
        val details = service.getDetails(paymentId, token);
        val returnDto = service.returnCreate(token, details.getAvailableReturnAmount(), UUID.randomUUID().toString());
        val returnId = returnDto.getQrReturnId();
        showQr(returnDto.getQrToken(), scaledAmount);
        val context = new Context(returnId, refundRequest.getPaymentCallback());
        context.setType(Context.Type.RETURN);
        context.setDetails(details);
        process(context);
    }

    @Override
    public void process(CancelRequest cancelRequest, int amount) {
        throw new UnsupportedOperationException("Cancel is not implemented");
    }

    private void process(Context context) {
        if (service.isNonExpired(context)) {
            logger.trace("Context is not expired: {}", context);
            processNextStep(context);
        } else {
            logger.trace("Context is expired: {}", context);
            processExpired(context);
        }
    }

    private void processNextStep(Context context) {
        logger.trace("Processing next step: {}", context);

        if (Objects.isNull(context)) {
            logger.error("Context is null!");
            return;
        }

        PaymentStatus paymentStatus = context.getStatus();

        if (paymentStatus.isNonFinal()) {
            try {
                service.updateStatus(context);
                logger.trace("Status updated: {}", context);
                paymentStatus = context.getStatus();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                process(context);
                return;
            }
        }
        logger.trace("Processing status: {}", paymentStatus);

        switch (paymentStatus) {
            case CUSTOMER_IDENTITY_OBTAINED:
                ReturnData returnData = service.returnPayment(token, getTransactionAmount(), context.getDetails().getQrPaymentId(), context.getId());
                context.setReturnData(returnData);
                processNextStep(context);
                return;
            case PROCESSED:
                try {
                    PaymentDetails details = service.getDetails(context.getId(), token);
                    context.setDetails(details);
                    logger.trace("Got details: {}", details);
                } catch (Exception e) {
                    logger.warn("Не удалось получить детали", e);
                }
                if (context.getDetails() != null && context.getStatusData() != null) {
                    processSuccess(context.getCallback(), context.getDetails(), context.getStatusData());
                    return;
                }
                processNextStep(context);
                return;
            case ERROR:
                processFailed(context);
                return;
            case WAIT:
                if (!context.checkIsStatusProcessed(PaymentStatus.WAIT)) {
                    logger.trace("Wait status not processed: {}", context);
                    uiService.showSpinner(WAIT_CONFIRM_MESSAGE);
                    display.clear();
                    display.display(new CustomerDisplayMessage(WAIT_CONFIRM_MESSAGE, Duration.ofMinutes(1)));
                } else {
                    logger.trace("Wait status already processed: {}", context);
                }

            case UNKNOWN:
            default:
                process(context);
        }
        PaymentStatus finalPaymentStatus = paymentStatus;
        context.addProcessedStatus(finalPaymentStatus);
    }

    private void processFailed(Context context) {
        display.clear();
        String title = Optional.ofNullable(context.getMessage()).orElse(DEFAULT_ERROR_MESSAGE);
        display.display(new CustomerDisplayMessage(title, Duration.ofSeconds(3)));
        uiService.showError(
                title,
                () -> {
                    logger.trace("Processing failed: {}", context);
                    context.getCallback().paymentNotCompleted();
                    logger.trace("Processing failed - callback triggered");
                }
        );
    }

    private void processExpired(Context context) {
        display.clear();
        uiService.showDialog(
                "Произошла ошибка связи, проверьте, прошла ли операция на терминале?",
                () -> {
                    uiService.showSpinner("Выполняется запрос к процессингу Kaspi QR");
                    processNextStep(context);
                },
                () -> {
                    context.getCallback().paymentNotCompleted();
                    display.clear();
                }
        );
    }

    private void showQr(String paymentQrToken, BigDecimal scaledAmount) {

        switch (properties.getShowQrOn()) {
            case SLIP: {
                val slip = new Slip();
                val paragraphs = slip.getParagraphs();
                paragraphs.add(new SlipParagraph(SlipParagraphType.TEXT, SCAN_QR_TITLE_KZ));
                paragraphs.add(new SlipParagraph(SlipParagraphType.QR, paymentQrToken));
                paragraphs.add(new SlipParagraph(SlipParagraphType.TEXT, SCAN_QR_TITLE));
                try {
                    printer.print(slip);
                } catch (SetApiPrinterException e) {
                    throw new BaseError(e);
                }
                break;
            }
            case CASHIER_DISPLAY: {
                uiService.showQrCode(paymentQrToken, SCAN_QR_TITLE, scaledAmount);
                break;
            }
            case CUSTOMER_DISPLAY:
            default: {
                display.clear();
                CustomerDisplayMessage message = new CustomerDisplayMessage(SCAN_QR_TITLE, paymentQrToken, scaledAmount);
                CommunicationMessage communicationMessage = message.getMessage();
                communicationMessage.setHeader(SCAN_QR_TITLE_KZ);
                display.display(message);
                logger.trace("slip showed for qr {}", paymentQrToken);
                break;
            }
        }
    }

    private void showInitMessage() {

        switch (properties.getShowQrOn()) {
            case SLIP: {
                break;
            }
            case CASHIER_DISPLAY: {
                uiService.showSpinner(INIT_MESSAGE);
                break;
            }
            case CUSTOMER_DISPLAY:
            default: {
                display.clear();
                CustomerDisplayMessage message = new CustomerDisplayMessage(INIT_MESSAGE_RU, Duration.ZERO);
                CommunicationMessage communicationMessage = message.getMessage();
                communicationMessage.setHeader(INIT_MESSAGE_KZ);
                display.display(message);
                break;
            }
        }
    }

    private void showSuccessMessage(BigDecimal amount, Runnable onComplete) {

        CustomerDisplayMessage message = new CustomerDisplayMessage(SUCCESS_MESSAGE_RU, Duration.ofSeconds(3));
        CommunicationMessage communicationMessage = message.getMessage();
        communicationMessage.setSum(amount);
        communicationMessage.setHeader(SUCCESS_MESSAGE_KZ);

        display.clear();
        display.display(message);
        uiService.showTimingOut(SUCCESS_MESSAGE_KZ + "\n" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "\n" + SUCCESS_MESSAGE_RU, onComplete);
    }

    private void processSuccess(PaymentCallback callback, PaymentDetails details, PaymentStatusData statusData) {
        display.clear();

        val date = details.getTransactionDate().format(ISO_OFFSET_DATE_TIME);
        val payment = new Payment();
        val amount = details.getTotalAmount();
        payment.setSum(amount);
        val data = payment.getData();
        data.put(PAYMENT_ID, details.getQrPaymentId());
        String amountString = amount.setScale(2, RoundingMode.DOWN).toPlainString();
        data.put(AMOUNT, amountString);
        data.put(TERMINAL_NUMBER, terminalId);
        data.put(TRANSACTION_DATE, date);
        data.put(BANK_ID, KASPI_QR_BANK_ID);
        data.put(PAYMENT_METHOD, METHOD);

        if (Objects.nonNull(statusData)) {
            Optional.ofNullable(statusData.getAddress()).ifPresent(it -> data.put(ADDRESS, it));
            Optional.ofNullable(statusData.getCity()).ifPresent(it -> data.put(CITY, it));
            Optional.ofNullable(statusData.getStoreName()).ifPresent(it -> data.put(STORE_NAME, it));
            Optional.ofNullable(statusData.getLoanTerm()).ifPresent(it -> data.put(LOAN_OFFER_NAME, it));
            Optional.ofNullable(statusData.getIsOffer()).ifPresent(it -> data.put(IS_OFFER, it.toString()));
            Optional.ofNullable(statusData.getLoanTerm()).ifPresent(it -> data.put(LOAN_TERM, it));
            Optional.ofNullable(statusData.getProductType()).ifPresent(it -> data.put(PRODUCT_TYPE, it));
        }

        val slips = payment.getSlips();
        SlipProperties slipProperties = new SlipProperties(
                METHOD,
                terminalId,
                date,
                amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
        slipProperties.setAddress(statusData.getAddress());
        slipProperties.setCity(statusData.getCity());
        slipProperties.setStoreName(statusData.getStoreName());

        slips.add(buildSlip(slipProperties));

        showSuccessMessage(amount, () -> {
            try {
                callback.paymentCompleted(payment);
            } catch (InvalidPaymentException e) {
                logger.error(e.getMessage(), e);
                throw new BaseError(e);
            }
        });

    }
}
