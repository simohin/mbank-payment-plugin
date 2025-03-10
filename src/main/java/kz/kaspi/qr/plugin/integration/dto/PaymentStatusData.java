package kz.kaspi.qr.plugin.integration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentStatusData {
    private PaymentStatus status;
    private String amount;
    private String storeName;
    private String address;
    private String city;
    private String loanOfferName;
    private String loanTerm;
    private Boolean isOffer;
    private String productType;
}
