package kz.kaspi.qr.plugin;

import common.config.CustomerDisplayConfig;
import common.config.PluginConfig;
import common.config.PluginConfigProperties;
import common.config.PrinterConfig;
import common.exception.BaseError;
import common.service.BankIntegrationService;
import common.service.slip.SlipProperties;
import kz.kaspi.qr.plugin.integration.Context;
import kz.kaspi.qr.plugin.integration.KaspiQRPayService;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static common.config.LogConfig.getLogger;
import static common.service.slip.SlipService.buildSlip;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class KaspiQrIntegrationService implements BankIntegrationService, ShiftEventListener {

    public static final String KASPI_QR_BANK_ID = "KASPIQR";
    public static final String PAYMENT_ID = "kaspi.qr.payment.id";
    public static final String TERMINAL_NUMBER = "kaspi.qr.terminal.number";
    public static final String TRANSACTION_DATE = "kaspi.qr.transaction.date";
    public static final String BANK_ID = "kaspi.qr.bank.id";
    public static final String PAYMENT_METHOD = "kaspi.qr.payment.method";
    public static final String METHOD = "Kaspi QR";
    private final Logger logger = getLogger();
    private final KaspiQRPayService service = new KaspiQRPayService();
    private final SetApiPrinter printer = PrinterConfig.getPrinter();
    private final CustomerDisplay display = CustomerDisplayConfig.getDisplay();
    private final PluginConfigProperties properties = PluginConfig.getProperties();
    private final String token;
    private final String terminalId;

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
        showQr(payment, scaledAmount);
        Context context = new Context(payment.getQrPaymentId(), paymentRequest.getPaymentCallback());
        service.pollPaymentStatus(context);
        val details = service.getDetails(payment.getQrPaymentId(), token);
        processSuccess(paymentRequest.getPaymentCallback(), details);
    }

    private void showQr(kz.kaspi.qr.plugin.integration.dto.Payment payment, BigDecimal scaledAmount) {
        val title = "Оплата по QR. PaymentId: ";

        if (properties.isShowQrOnClientDisplay()) {
            display.clear();
            display.display(new CustomerDisplayMessage(title + payment.getQrPaymentId(), payment.getQrToken(), scaledAmount));
            logger.trace("slip showed for qr {}", payment.getQrToken());
        } else {
            val slip = new Slip();
            val paragraphs = slip.getParagraphs();
            paragraphs.add(new SlipParagraph(SlipParagraphType.TEXT, title + payment.getQrPaymentId()));
            paragraphs.add(new SlipParagraph(SlipParagraphType.QR, payment.getQrToken()));
            try {
                printer.print(slip);
            } catch (SetApiPrinterException e) {
                throw new BaseError(e);
            }
            logger.trace("slip printed for qr {}", payment.getQrToken());
        }
    }

    private void processSuccess(PaymentCallback callback, PaymentDetails details) {

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

        val slips = payment.getSlips();
        slips.add(
                buildSlip(new SlipProperties(
                        METHOD,
                        terminalId,
                        date,
                        amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
                )));

        try {
            callback.paymentCompleted(payment);
        } catch (InvalidPaymentException e) {
            throw new BaseError(e);
        }
    }

    @Override
    public void process(RefundRequest refundRequest, int amount) {
        val paymentId = refundRequest.getOriginalPayment().getData().get(PAYMENT_ID);
        val details = service.getDetails(paymentId, token);
        val returnId = service.returnCreate(token, details.getAvailableReturnAmount(), UUID.randomUUID().toString());
        Context context = new Context(returnId, refundRequest.getPaymentCallback());
        service.pollReturnStatus(context);
        processSuccess(refundRequest.getPaymentCallback(), service.getDetails(returnId, token));
    }

    @Override
    public void process(CancelRequest cancelRequest, int amount) {
        throw new UnsupportedOperationException("Cancel is not implemented");
    }
}
