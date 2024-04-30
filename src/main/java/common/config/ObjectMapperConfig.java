package common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperConfig {
    private static ObjectMapper INSTANCE;

    private ObjectMapperConfig() {
    }

    public static ObjectMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .setPropertyNamingStrategy(PropertyNamingStrategies.UpperCamelCaseStrategy.INSTANCE)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        return INSTANCE;
    }

}
