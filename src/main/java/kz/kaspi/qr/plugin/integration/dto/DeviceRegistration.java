package kz.kaspi.qr.plugin.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistration {
    private String tradePointId;
    private String deviceId;
}
