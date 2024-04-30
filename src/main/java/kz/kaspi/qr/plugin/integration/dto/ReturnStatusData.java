package kz.kaspi.qr.plugin.integration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReturnStatusData {
    private PaymentStatus status;
}
