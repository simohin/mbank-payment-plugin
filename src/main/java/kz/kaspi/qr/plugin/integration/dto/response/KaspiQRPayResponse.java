package kz.kaspi.qr.plugin.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.kaspi.qr.plugin.integration.dto.StatusCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class KaspiQRPayResponse<T> {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private StatusCode statusCode;
    private String message;
    private T data;
}
