package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static kz.kaspi.qr.plugin.integration.Constants.DATE_PATTERN;

@Data
@NoArgsConstructor
public class PaymentDetails {
    private String qrPaymentId;
    private BigDecimal totalAmount;
    private BigDecimal availableReturnAmount;
    @JsonFormat(pattern = DATE_PATTERN)
    private OffsetDateTime transactionDate;
}
