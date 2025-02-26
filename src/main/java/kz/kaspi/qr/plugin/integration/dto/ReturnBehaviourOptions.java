package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReturnBehaviourOptions {
    @JsonProperty("qrCodeScanEventPollingInterval")
    @JsonAlias("QrCodeScanEventPollingInterval")
    private long qrCodeScanEventPollingInterval;

    @JsonProperty("qrCodeScanWaitTimeout")
    @JsonAlias("QrCodeScanWaitTimeout")
    private long qrCodeScanWaitTimeout;
}
