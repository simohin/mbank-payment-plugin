package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    @JsonProperty("Unknown")
    UNKNOWN,
    @JsonProperty("QrTokenCreated")
    CREATED,
    @JsonProperty("CustomerIdentityObtained")
    CUSTOMER_IDENTITY_OBTAINED,
    @JsonProperty("Wait")
    WAIT,
    @JsonProperty("Processed")
    PROCESSED,
    @JsonProperty("Error")
    ERROR;

    public boolean isNonFinal() {
        switch (this) {
            case PROCESSED:
            case ERROR:
                return false;
            default:
                return true;
        }
    }

    public boolean isSuccess() {
        return this.equals(PROCESSED);
    }
}
