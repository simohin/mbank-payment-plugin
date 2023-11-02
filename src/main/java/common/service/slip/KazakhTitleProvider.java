package common.service.slip;

public class KazakhTitleProvider implements SlipTitleProvider {
    @Override
    public String type() {
        return "Транзакция түрі";
    }

    @Override
    public String city() {
        return "Қала";
    }

    @Override
    public String method() {
        return "Төлем тәсілі";
    }

    @Override
    public String storeName() {
        return "Дүкен атауы";
    }

    @Override
    public String bin() {
        return "БСН";
    }

    @Override
    public String address() {
        return "Мекен-жайы";
    }

    @Override
    public String terminalId() {
        return "Терминал";
    }

    @Override
    public String date() {
        return "Күні";
    }

    @Override
    public String orderNumber() {
        return "Транзакция нөмірі";
    }

    @Override
    public String cardMask() {
        return "Карта маскасы";
    }

    @Override
    public String authorizationCode() {
        return "Авторизация коды";
    }

    @Override
    public String amount() {
        return "Сомасы";
    }

    @Override
    public String status() {
        return "Мәртебесі";
    }

    @Override
    public String hostResponseCode() {
        return "Жауап коды";
    }
}
