package common.service;

import ru.crystals.pos.api.ext.loyal.dto.DailyReportResult;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;

public interface BankIntegrationService {
    void process(PaymentRequest request, int amount);

    void process(RefundRequest request, int amount);

    void process(CancelRequest request, int amount);

    default DailyReportResult buildDailyReport() {
        return null;
    }
}
