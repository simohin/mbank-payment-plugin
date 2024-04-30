package common.service.slip;

public class SlipProperties {

    private String type;
    private String method;
    private String storeName;
    private String bin;
    private String address;
    private String terminalId;
    private String date;
    private String orderNumber;
    private String cardMask;
    private String icc;
    private String rrn;
    private String authorizationCode;
    private String amount;
    private String status;
    private String hostResponseCode;
    private String city;

    public SlipProperties() {
    }

    public SlipProperties(String method, String terminalId, String date, String amount) {
        this(
                null,
                method,
                null,
                null,
                null,
                terminalId,
                date,
                null,
                null,
                null,
                null,
                null,
                amount,
                null,
                null,
                null
        );
    }

    public SlipProperties(String type, String method, String storeName, String bin, String address, String terminalId, String date, String orderNumber, String cardMask, String icc, String rrn, String authorizationCode, String amount, String status, String hostResponseCode, String city) {
        this.type = type;
        this.method = method;
        this.storeName = storeName;
        this.bin = bin;
        this.address = address;
        this.terminalId = terminalId;
        this.date = date;
        this.orderNumber = orderNumber;
        this.cardMask = cardMask;
        this.icc = icc;
        this.rrn = rrn;
        this.authorizationCode = authorizationCode;
        this.amount = amount;
        this.status = status;
        this.hostResponseCode = hostResponseCode;
        this.city = city;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCardMask() {
        return cardMask;
    }

    public void setCardMask(String cardMask) {
        this.cardMask = cardMask;
    }

    public String getIcc() {
        return icc;
    }

    public void setIcc(String icc) {
        this.icc = icc;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostResponseCode() {
        return hostResponseCode;
    }

    public void setHostResponseCode(String hostResponseCode) {
        this.hostResponseCode = hostResponseCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
