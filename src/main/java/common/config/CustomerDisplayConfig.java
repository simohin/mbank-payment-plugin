package common.config;

import ru.crystals.pos.spi.equipment.CustomerDisplay;

public final class CustomerDisplayConfig {
    private static CustomerDisplay DISPLAY;

    private CustomerDisplayConfig() {
    }

    public static void init(CustomerDisplay display) {
        if (DISPLAY != null) {
            return;
        }

        DISPLAY = display;
    }

    public static CustomerDisplay getDisplay() {
        if (DISPLAY == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return DISPLAY;
    }
}
