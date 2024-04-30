package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;

import static kz.kaspi.qr.plugin.integration.Constants.DATE_PATTERN;

@Data
@NoArgsConstructor
public class Payment {
    private String qrToken;
    private String qrPaymentId;
    private Set<PaymentMethod> paymentMethods;
    @JsonFormat(pattern = DATE_PATTERN)
    private OffsetDateTime expireDate;
    private PaymentBehaviourOptions qrPaymentBehaviorOptions;
}
