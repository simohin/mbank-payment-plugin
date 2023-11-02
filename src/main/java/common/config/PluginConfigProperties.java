package common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import common.model.Language;

@Setter
@Getter
@ToString
public final class PluginConfigProperties {
    @Getter
    @JsonProperty(defaultValue = "false")
    private boolean ownCheque;
    @JsonProperty(defaultValue = "true")
    private boolean printSlip;
    @JsonProperty(defaultValue = "true")
    private boolean dailyReportsEnabled;
    @JsonProperty(defaultValue = "http://127.0.0.1:8080")
    private String ip;
    @JsonProperty(defaultValue = "180000")
    private int readTimeout;
    @JsonProperty(defaultValue = "5000")
    private int connectionTimeout;
    @JsonProperty(defaultValue = "181000")
    private int transactionTime;
    @JsonProperty(defaultValue = "false")
    private boolean showSumEnterForm;
    @JsonProperty(defaultValue = "false")
    private boolean manualConfirmation;
    @JsonProperty(defaultValue = "MIX")
    private Language language;
    @JsonProperty(defaultValue = "00000000")
    private String systemPassword;
    @JsonProperty(defaultValue = "true")
    private boolean activeSumEnterFormPayment;
    @JsonProperty(defaultValue = "true")
    private boolean activeSumEnterFormRefund;
}
