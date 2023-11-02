package common.service.slip;

public class RussianTitleProvider implements SlipTitleProvider {
    @Override
    public String type() {
        return "Тип операции";
    }

    @Override
    public String city() {
        return "Город";
    }

    @Override
    public String method() {
        return "Способ оплаты";
    }

    @Override
    public String storeName() {
        return "Название магазина";
    }

    @Override
    public String bin() {
        return "БИН";
    }

    @Override
    public String address() {
        return "Адрес";
    }

    @Override
    public String terminalId() {
        return "Терминал";
    }

    @Override
    public String date() {
        return "Дата";
    }

    @Override
    public String orderNumber() {
        return "Номер транзакции";
    }

    @Override
    public String cardMask() {
        return "Маска карты";
    }

    @Override
    public String authorizationCode() {
        return "Код авторизации";
    }

    @Override
    public String amount() {
        return "Сумма";
    }

    @Override
    public String status() {
        return "Статус";
    }

    @Override
    public String hostResponseCode() {
        return "Код ответа";
    }
}
