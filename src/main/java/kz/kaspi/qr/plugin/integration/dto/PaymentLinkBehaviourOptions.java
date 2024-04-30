package kz.kaspi.qr.plugin.integration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentLinkBehaviourOptions {
    private long statusPollingInterval;
    private long linkActivationWaitTimeout;
    private long paymentConfirmationTimeout;
}
