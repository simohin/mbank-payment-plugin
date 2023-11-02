package common.config;

import ru.crystals.pos.spi.equipment.SetApiPrinter;

public final class PrinterConfig {
    private static SetApiPrinter PRINTER;

    private PrinterConfig() {
    }

    public static void init(SetApiPrinter printer) {
        if (PRINTER != null) {
            return;
        }

        PRINTER = printer;
    }

    public static SetApiPrinter getPrinter() {
        if (PRINTER == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return PRINTER;
    }
}
