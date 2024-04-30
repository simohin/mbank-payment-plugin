package common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import common.model.Language;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public final class PluginConfigProperties {
    @JsonProperty(defaultValue = "true")
    private boolean dailyReportsEnabled;
    @JsonProperty(defaultValue = "http://127.0.0.1:8080")
    private String ip;
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
    @JsonProperty(defaultValue = "true")
    private boolean showQrOnClientDisplay;
    @JsonProperty
    private String rootCaPath;
    @JsonProperty
    private String caPath;
    @JsonProperty
    private String certPath;
    @JsonProperty
    private String certPassword;
    @JsonProperty
    private String tradePointId;
    @JsonProperty
    private String deviceId;
}
