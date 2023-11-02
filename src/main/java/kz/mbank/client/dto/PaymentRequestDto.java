package kz.mbank.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class PaymentRequestDto implements HasTimeOutFlag {
    @JsonProperty("branch_number")
    private Integer branchNumber = 2;
    @JsonProperty("cashbox_number")
    private Integer cashboxNumber = 1;
    @JsonProperty("shift_number")
    private Integer shiftNumber = 2;
    @JsonProperty("cashier_name")
    private String cashierName = "Test";
    @JsonProperty("cheque_number")
    private Integer chequeNumber = 1;
    @JsonProperty("cheque_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DD hh:mm:ss")
    private OffsetDateTime chequeDate = OffsetDateTime.now();
    @JsonProperty("comment")
    private String comment = "Test";
    @JsonProperty("payment_type")
    private PaymentType paymentType = PaymentType.QR;
    @JsonProperty("is_time_out")
    private Boolean isTimeOut = false;
    @JsonProperty("_sum")
    private BigDecimal amount;
}
