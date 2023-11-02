package common.service;

import org.slf4j.Logger;
import ru.crystals.pos.spi.ResBundle;
import ru.crystals.pos.spi.equipment.SetApiPrinter;
import ru.crystals.pos.spi.ui.UIForms;
import common.config.PluginConfigProperties;

import static common.config.BankIntegrationConfig.getInstance;
import static common.config.LogConfig.getLogger;
import static common.config.PluginConfig.getProperties;
import static common.config.PrinterConfig.getPrinter;
import static common.config.ResBundleConfig.getResBundle;
import static common.config.UIConfig.getUiForms;
import static common.config.UIConfig.getUiService;


public abstract class SharedResourcesService {
    protected final UIForms ui = getUiForms();
    protected final SetApiPrinter printer = getPrinter();
    protected final Logger log = getLogger();
    protected final ResBundle res = getResBundle();
    protected final PluginConfigProperties configProperties = getProperties();
    protected final UIService uiService = getUiService();
    protected final BankIntegrationService bankIntegrationService = getInstance();

}
