package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kz.kaspi.qr.plugin.integration.PriceSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreate extends Create {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal amount;

    public PaymentCreate(final String deviceToken, final String externalId, final BigDecimal amount) {
        super(deviceToken, externalId);
        this.amount = amount;
    }
}
