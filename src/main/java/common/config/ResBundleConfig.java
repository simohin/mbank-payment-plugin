package common.config;

import ru.crystals.pos.spi.ResBundle;

public final class ResBundleConfig {
    private static ResBundle RES;

    private ResBundleConfig() {
    }

    public static void init(ResBundle res) {
        if (RES != null) {
            return;
        }

        RES = res;
    }

    public static ResBundle getResBundle() {
        if (RES == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return RES;
    }
}
