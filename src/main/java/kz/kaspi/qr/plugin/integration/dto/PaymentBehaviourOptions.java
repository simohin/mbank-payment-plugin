package kz.kaspi.qr.plugin.integration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentBehaviourOptions {
    private long statusPollingInterval;
    private long qrCodeScanWaitTimeout;
    private long paymentConfirmationTimeout;
}
