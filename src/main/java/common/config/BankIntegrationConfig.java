package common.config;

import common.service.BankIntegrationService;

public class BankIntegrationConfig {
    private static BankIntegrationService INSTANCE;

    private BankIntegrationConfig() {
    }

    public static void init(BankIntegrationService service) {
        if (INSTANCE != null) {
            return;
        }

        INSTANCE = service;
    }

    public static BankIntegrationService getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return INSTANCE;
    }
}
