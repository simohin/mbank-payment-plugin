package common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.BaseError;
import org.slf4j.Logger;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.ResBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PluginConfig {

    private static final ObjectMapper MAPPER = ObjectMapperConfig.getInstance();

    private static PluginConfigProperties PROPERTIES;

    private static final Logger LOGGER = LogConfig.getLogger();

    private PluginConfig() {
    }

    public static void init(IntegrationProperties properties, ResBundle resBundle) {
        String os = System.getProperty("os.name");
        String configFilePath = resBundle.getString("config.file.path.linux");
        if (os.toLowerCase().startsWith("windows")) {
            configFilePath = resBundle.getString("config.file.path.windows");
        }

        String configPathString = properties.getServiceProperties().get(configFilePath);
        Path configPath;
        try {
            configPath = Paths.get(configPathString);
        } catch (Throwable e) {
            LOGGER.debug("Config file not found by path {}, initializing with default", configPathString);
            try {
                PROPERTIES = MAPPER.readValue("", PluginConfigProperties.class);
            } catch (IOException ex) {
                throw new BaseError(ex);
            }
            return;
        }

        String configFileContent;
        try {
            configFileContent = new String(Files.readAllBytes(configPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            LOGGER.debug("Config file content {}", configFileContent);
            PROPERTIES = MAPPER.readValue(configFileContent, PluginConfigProperties.class);
        } catch (Throwable e) {
            throw new BaseError("Error on reading configs", e);
        }
        LOGGER.debug("Plugin initialized with {}", PROPERTIES.toString());
    }

    public static PluginConfigProperties getProperties() {
        if (PROPERTIES == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return PROPERTIES;
    }

    public static boolean isPluginAvailable() {
        if (PROPERTIES == null) return false;
        String ip = PROPERTIES.getIp();
        return ip != null && !ip.isEmpty();
    }

    public static boolean hasDailyReports() {
        if (PROPERTIES == null) return false;
        return PROPERTIES.isDailyReportsEnabled();
    }

}
