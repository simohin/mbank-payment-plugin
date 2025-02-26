package kz.kaspi.qr.plugin.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnPaymentRequest {
    private String deviceToken;
    private String qrPaymentId;
    private String qrReturnId;
    private Double amount;
}
