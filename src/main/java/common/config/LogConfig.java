package common.config;

import org.slf4j.Logger;

public final class LogConfig {
    private static Logger LOGGER;

    private LogConfig() {
    }

    public static void init(Logger logger) {
        if (LOGGER != null) {
            return;
        }

        LOGGER = logger;
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return LOGGER;
    }
}
