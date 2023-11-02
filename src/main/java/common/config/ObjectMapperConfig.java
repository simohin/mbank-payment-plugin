package common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class ObjectMapperConfig {
    private static ObjectMapper INSTANCE;
    private ObjectMapperConfig() {
    }

    public static ObjectMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        return INSTANCE;
    }

}
