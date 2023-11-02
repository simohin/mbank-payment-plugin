package common.service;

import org.slf4j.Logger;
import common.config.LogConfig;

import java.math.BigDecimal;

public class TransactionAmountHandler {

    private static final Logger log = LogConfig.getLogger();
    private static final ThreadLocal<BigDecimal> AMOUNT = new ThreadLocal<>();

    public static void setTransactionAmount(BigDecimal amount) {
        AMOUNT.set(amount);
        log.debug("Transaction amount {}", AMOUNT.get());
    }

    public static BigDecimal getTransactionAmount() {
        return AMOUNT.get();
    }
}
