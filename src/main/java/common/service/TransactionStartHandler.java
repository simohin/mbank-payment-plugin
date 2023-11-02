package common.service;

import common.config.LogConfig;
import common.config.PluginConfig;
import common.config.PluginConfigProperties;
import org.slf4j.Logger;

public class TransactionStartHandler {

    private static final Logger log = LogConfig.getLogger();
    private static final PluginConfigProperties properties = PluginConfig.getProperties();
    private static final ThreadLocal<Long> TRANSACTION_START = new ThreadLocal<>();

    public static void setTransactionStart() {
        TRANSACTION_START.set(System.currentTimeMillis());
        log.debug("Transaction start {}", TRANSACTION_START.get());
    }

    public static long getTransactionStart() {
        return TRANSACTION_START.get();
    }

    public static boolean checkTransactionExpired() {
        long start = getTransactionStart();
        long now = System.currentTimeMillis();
        log.trace("checkTransactionExpired start: {} now: {}", start, now);
        return (now - start) > properties.getTransactionTime();
    }
}
