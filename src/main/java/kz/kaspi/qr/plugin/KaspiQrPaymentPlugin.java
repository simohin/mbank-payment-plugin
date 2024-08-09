package kz.kaspi.qr.plugin;

import common.config.BankIntegrationConfig;
import common.config.CustomerDisplayConfig;
import common.config.LogConfig;
import common.config.PluginConfig;
import common.config.PrinterConfig;
import common.config.ResBundleConfig;
import common.config.UIConfig;
import common.service.CancelService;
import common.service.PaymentService;
import common.service.RefundService;
import kz.kaspi.qr.plugin.integration.config.ClientConfig;
import org.slf4j.Logger;
import ru.crystals.pos.api.plugin.PaymentPlugin;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.POSInfo;
import ru.crystals.pos.spi.ResBundle;
import ru.crystals.pos.spi.annotation.Inject;
import ru.crystals.pos.spi.annotation.POSPlugin;
import ru.crystals.pos.spi.equipment.CustomerDisplay;
import ru.crystals.pos.spi.equipment.SetApiPrinter;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import ru.crystals.pos.spi.ui.UIForms;

import javax.annotation.PostConstruct;

import static common.util.ErrorHandlingUtil.runWithHandling;

@POSPlugin(id = "kaspi.qr.payment.plugin")
public class KaspiQrPaymentPlugin implements PaymentPlugin {

    @Inject
    private POSInfo pos;
    @Inject
    private CustomerDisplay display;
    @Inject
    private IntegrationProperties properties;
    @Inject
    private UIForms ui;
    @Inject
    private Logger logger;
    @Inject
    private ResBundle resources;
    @Inject
    private SetApiPrinter printer;
    private PaymentService paymentService;
    private RefundService refundService;
    private CancelService cancelService;

    @PostConstruct
    private void init() {
        try {
            initSharedResources();
            initServices();
        } catch (Exception e) {
            logger.debug("Failed to init", e);
            throw e;
        }
    }

    private void initSharedResources() {
        CustomerDisplayConfig.init(display);
        LogConfig.init(logger);
        ResBundleConfig.init(resources);
        PluginConfig.init(properties);
        UIConfig.init(ui);
        PrinterConfig.init(printer);
        logger.debug("Shared resources loaded");
    }

    private void initServices() {
        ClientConfig.init();
        BankIntegrationConfig.init(new KaspiQrIntegrationService());
        paymentService = new PaymentService();
        refundService = new RefundService();
        cancelService = new CancelService();
        logger.debug("Services loaded");
    }

    @Override
    public void doPayment(PaymentRequest request) {
        runWithHandling(
                () -> paymentService.process(request),
                request.getPaymentCallback()
        );
    }

    @Override
    public void doPaymentCancel(CancelRequest request) {
        runWithHandling(
                () -> cancelService.process(request),
                request.getPaymentCallback()
        );
    }

    @Override
    public void doRefund(RefundRequest request) {
        runWithHandling(
                () -> refundService.process(request),
                request.getPaymentCallback()
        );
    }

    @Override
    public boolean isAvailable() {
        return PluginConfig.isPluginAvailable();
    }

    @Override
    public boolean hasDailyReports() {
        return PluginConfig.hasDailyReports();
    }
}
