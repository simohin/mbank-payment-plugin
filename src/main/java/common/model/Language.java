package common.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Language {
    RUS,
    KAZ,
    MIX;

    @JsonCreator
    public static Language of(String value) {
        return Arrays.stream(Language.values())
                .filter(it -> it.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(MIX);
    }
}
