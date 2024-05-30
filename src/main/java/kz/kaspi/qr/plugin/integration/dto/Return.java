package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Return {
    private String qrToken;
    private String qrReturnId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime expireDate;
    private ReturnBehaviourOptions qrReturnBehaviorOptions;
}
