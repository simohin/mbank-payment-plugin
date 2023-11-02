package common.service.slip;

public interface SlipTitleProvider {

    default String type() {
        return "Транзакция түрі / Тип операции";
    }

    default String method() {
        return "Төлем тәсілі / Способ оплаты";
    }

    default String storeName() {
        return "Дүкен атауы / Название магазина";
    }

    default String bin() {
        return "БСН / БИН";
    }

    default String address() {
        return "Мекен-жайы / Адрес";
    }

    default String terminalId() {
        return "Терминал / Терминал";
    }

    default String date() {
        return "Күні / Дата";
    }

    default String orderNumber() {
        return "Транзакция нөмірі / Номер транзакции";
    }

    default String cardMask() {
        return "Карта маскасы / Маска карты";
    }

    default String icc() {
        return "ICC";
    }

    default String rrn() {
        return "RRN";
    }

    default String authorizationCode() {
        return "Авторизация коды / Код авторизации";
    }

    default String amount() {
        return "Сомасы / Сумма";
    }

    default String status() {
        return "Мәртебесі / Статус";
    }

    default String hostResponseCode() {
        return "Жауап коды / Код ответа";
    }

    default String city() {
        return "Қала / Город";
    }
}
