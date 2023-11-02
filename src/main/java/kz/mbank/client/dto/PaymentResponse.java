package kz.mbank.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.omg.PortableInterceptor.SUCCESSFUL;

import java.util.Arrays;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class PaymentResponse {

    private Integer code;
    private String message;

    public boolean isSuccess() {
        return Code.SUCCESS.equals(Code.of(code));
    }

    @RequiredArgsConstructor
    public enum Code {
        SUCCESS(200),
        UNKNOWN(-1);

        private final int value;

        private static final Code[] VALUES = Code.values();

        public static Code of(Integer value) {
            return Arrays.stream(VALUES).filter(it -> value.equals(it.value))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }
}
