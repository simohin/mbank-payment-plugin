package common.config;

import common.service.UIService;
import ru.crystals.pos.spi.ui.UIForms;

public final class UIConfig {
    private static UIForms UI_FORMS;
    private static UIService UI_SERVICE;

    private UIConfig() {
    }

    public static void init(UIForms ui) {
        if (UI_FORMS != null) {
            return;
        }

        UI_FORMS = ui;
    }

    public static UIForms getUiForms() {
        if (UI_FORMS == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return UI_FORMS;
    }

    public static UIService getUiService() {
        if (UI_SERVICE == null) {
            UI_SERVICE = new UIService();
        }
        return UI_SERVICE;
    }
}
