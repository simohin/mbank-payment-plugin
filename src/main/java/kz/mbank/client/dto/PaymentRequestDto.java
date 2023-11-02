package kz.mbank.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class PaymentRequestDto implements HasTimeOutFlag {
    private Integer branchNumber = 2;
    private Integer cashboxNumber = 1;
    private Integer shiftNumber = 2;
    private String cashierName = "Test";
    private Integer chequeNumber = 1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DD hh:mm:ss")
    private OffsetDateTime chequeDate = OffsetDateTime.now();
    private String comment = "Test";
    private PaymentType paymentType = PaymentType.QR;
    private Boolean isTimeOut = false;
    @JsonProperty("_sum")
    private BigDecimal amount;
}
