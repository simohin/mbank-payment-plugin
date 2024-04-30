package common.service;

import common.config.LogConfig;
import org.slf4j.Logger;

public class TransactionStartHandler {

    private static final Logger log = LogConfig.getLogger();
    private static final ThreadLocal<Long> TRANSACTION_START = new ThreadLocal<>();

    public static void setTransactionStart() {
        TRANSACTION_START.set(System.currentTimeMillis());
        log.debug("Transaction start {}", TRANSACTION_START.get());
    }

}
