package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kz.kaspi.qr.plugin.integration.PriceSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Create {
    private String deviceToken;
    private String externalId;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal amount;
}
