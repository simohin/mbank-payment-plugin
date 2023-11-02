package common.util;

import common.exception.ValidationError;

import java.math.BigDecimal;

public class ValidationUtil {

    public static void validateWholeNumber(BigDecimal amount) {
        if (amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationError("Сумма оплаты должна быть кратна 1,00 тенге. Укажите верную сумму и повторите оплату");
        }
    }
}
