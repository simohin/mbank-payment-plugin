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

    public String getStoreName() {
        return storeName;
    }

    public String getBin() {
        return bin;
    }

    public String getAddress() {
        return address;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getDate() {
        return date;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCardMask() {
        return cardMask;
    }

    public String getIcc() {
        return icc;
    }

    public String getRrn() {
        return rrn;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getHostResponseCode() {
        return hostResponseCode;
    }

    public String getCity() {
        return city;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setCardMask(String cardMask) {
        this.cardMask = cardMask;
    }

    public void setIcc(String icc) {
        this.icc = icc;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHostResponseCode(String hostResponseCode) {
        this.hostResponseCode = hostResponseCode;
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
