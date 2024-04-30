package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReturnStatus {
    @JsonProperty("QrTokenCreated")
    CREATED,
    @JsonProperty("CustomerIdentityObtained")
    ID_OBTAINED,
    @JsonProperty("Error")
    ERROR
}
