package kz.kaspi.qr.plugin.integration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReturnBehaviourOptions {
    private long qrCodeScanEventPollingInterval;
    private long qrCodeScanWaitTimeout;
}
