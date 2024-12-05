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
import lombok.val;
import org.slf4j.Logger;
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
import java.util.Optional;
import java.util.UUID;

import static common.config.CustomerDisplayConfig.getDisplay;
import static common.config.LogConfig.getLogger;
import static common.config.UIConfig.getUiService;
import static common.service.slip.SlipService.buildSlip;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class KaspiQrIntegrationService implements BankIntegrationService, ShiftEventListener {

    public static final String KASPI_QR_BANK_ID = "KASPIQR";
    public static final String PAYMENT_ID = "kaspi.qr.payment.id";
    public static final String TERMINAL_NUMBER = "kaspi.qr.terminal.number";
    public static final String TRANSACTION_DATE = "kaspi.qr.transaction.date";
    public static final String BANK_ID = "kaspi.qr.bank.id";
    public static final String PAYMENT_METHOD = "kaspi.qr.payment.method";
    public static final String ADDRESS = "kaspi.qr.payment.address";
    public static final String CITY = "kaspi.qr.payment.city";
    public static final String STORE_NAME = "kaspi.qr.payment.store.name";
    public static final String LOAN_OFFER_NAME = "kaspi.qr.payment.loan.offer.name";
    public static final String LOAN_TERM = "kaspi.qr.payment.loan.term";
    public static final String IS_OFFER = "kaspi.qr.payment.is.offer";
    public static final String PRODUCT_TYPE = "kaspi.qr.payment.product.type";
    public static final String METHOD = "Kaspi QR";
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
        val scaledAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        val payment = service.paymentCreate(token, scaledAmount, UUID.randomUUID().toString());
        showQr(payment.getQrToken(), scaledAmount);
        val context = new Context(payment.getQrPaymentId(), paymentRequest.getPaymentCallback());
        process(context);
    }

    private void process(Context context) {
        if (service.isNonExpired(context)) {
            processNextStep(context);
        } else {
            processExpired(context);
        }
    }

    private void processNextStep(Context context) {
        try {
            service.updateStatus(context);
        } catch (IOException e) {
            process(context);
            return;
        }

        switch (Optional.ofNullable(context.getStatusData().getStatus()).orElse(PaymentStatus.UNKNOWN)) {
            case PROCESSED:
                try {
                    context.setDetails(service.getDetails(context.getId(), token));
                } catch (Exception e) {
                    logger.warn("Не удалось получить детали", e);
                }
                if (context.getDetails() != null && context.getStatusData() != null) {
                    processSuccess(context.getCallback(), context.getDetails(), context.getStatusData());
                    return;
                }
                processNextStep(context);
                break;
            case ERROR:
                processFailed(context);
                break;
            case WAIT:
                display.clear();
            case UNKNOWN:
            default:
                process(context);
        }

    }

    private void processFailed(Context context) {
        display.clear();
        uiService.showError(
                Optional.ofNullable(context.getMessage()).orElse("Оплата не прошла"),
                () -> context.getCallback().paymentNotCompleted()
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
        val title = "Сканируйте и платите через приложение Kaspi.kz";

        if (properties.isShowQrOnClientDisplay()) {
            display.clear();
            display.display(new CustomerDisplayMessage(title, paymentQrToken, scaledAmount));
            logger.trace("slip showed for qr {}", paymentQrToken);
        } else {
            val slip = new Slip();
            val paragraphs = slip.getParagraphs();
            paragraphs.add(new SlipParagraph(SlipParagraphType.TEXT, title));
            paragraphs.add(new SlipParagraph(SlipParagraphType.QR, paymentQrToken));
            try {
                printer.print(slip);
            } catch (SetApiPrinterException e) {
                throw new BaseError(e);
            }
        }
    }

    private void processSuccess(PaymentCallback callback, PaymentDetails details, PaymentStatusData statusData) {
        display.clear();

        val date = details.getTransactionDate().format(ISO_OFFSET_DATE_TIME);
        val payment = new Payment();
        val amount = details.getTotalAmount();
        payment.setSum(amount);
        val data = payment.getData();
        data.put(PAYMENT_ID, details.getQrPaymentId());
        data.put(TERMINAL_NUMBER, terminalId);
        data.put(TRANSACTION_DATE, date);
        data.put(BANK_ID, KASPI_QR_BANK_ID);
        data.put(PAYMENT_METHOD, METHOD);
        data.put(ADDRESS, statusData.getAddress());
        data.put(CITY, statusData.getCity());
        data.put(STORE_NAME, statusData.getStoreName());
        data.put(LOAN_OFFER_NAME, statusData.getLoanOfferName());
        data.put(IS_OFFER, statusData.getIsOffer().toString());
        data.put(LOAN_TERM, statusData.getLoanTerm());
        data.put(PRODUCT_TYPE, statusData.getProductType());

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

        try {
            callback.paymentCompleted(payment);
        } catch (InvalidPaymentException e) {
            throw new BaseError(e);
        }
    }

    @Override
    public void process(RefundRequest refundRequest, int amount) {
        val scaledAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        val paymentId = refundRequest.getOriginalPayment().getData().get(PAYMENT_ID);
        val details = service.getDetails(paymentId, token);
        val returnDto = service.returnCreate(token, details.getAvailableReturnAmount(), UUID.randomUUID().toString());
        val returnId = returnDto.getQrReturnId();
        showQr(returnDto.getQrToken(), scaledAmount);
        val context = new Context(returnId, refundRequest.getPaymentCallback());
        context.setType(Context.Type.RETURN);
        process(context);
    }

    @Override
    public void process(CancelRequest cancelRequest, int amount) {
        throw new UnsupportedOperationException("Cancel is not implemented");
    }
}
