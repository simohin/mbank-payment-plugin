package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentBehaviourOptions {

    @JsonProperty("statusPollingInterval")
    @JsonAlias("StatusPollingInterval")
    private long statusPollingInterval;

    @JsonProperty("qrCodeScanWaitTimeout")
    @JsonAlias("QrCodeScanWaitTimeout")
    private long qrCodeScanWaitTimeout;

    @JsonProperty("paymentConfirmationTimeout")
    @JsonAlias("PaymentConfirmationTimeout")
    private long paymentConfirmationTimeout;
}
