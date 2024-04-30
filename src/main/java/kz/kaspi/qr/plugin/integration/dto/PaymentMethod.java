package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentMethod {
    @JsonProperty("Red")
    RED,
    @JsonProperty("Gold")
    GOLD,
    @JsonProperty("Loan")
    LOAN,
    @JsonProperty("BusinessAccount")
    BUSINESS_ACCOUNT
}
