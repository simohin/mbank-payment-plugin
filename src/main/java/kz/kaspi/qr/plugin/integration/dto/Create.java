package kz.kaspi.qr.plugin.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Create {
    private String deviceToken;
    private String externalId;
}
