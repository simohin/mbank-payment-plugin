package common.service.slip;


import common.config.PluginConfig;
import common.model.Language;
import common.config.PrinterConfig;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.crystals.pos.spi.equipment.SetApiPrinter.PAPER_WIDTH_UNKNOWN;

public class SlipService {

    private static final int paperWidth = PrinterConfig.getPrinter().getPaperWidth();

    public static final SlipTitleProvider DEFAULT_PROVIDER = new SlipTitleProvider() {
    };

    private static final Map<Language, SlipTitleProvider> TITLE_PROVIDERS = new HashMap<Language, SlipTitleProvider>() {{
        put(Language.RUS, new RussianTitleProvider());
        put(Language.KAZ, new KazakhTitleProvider());
        put(Language.MIX, DEFAULT_PROVIDER);
    }};

    public static String buildSlip(SlipProperties info) {
        SlipTitleProvider provider = TITLE_PROVIDERS.getOrDefault(PluginConfig.getProperties().getLanguage(), DEFAULT_PROVIDER);

        HashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put(provider.type(), info.getType());
        parameters.put(provider.method(), info.getMethod());
        parameters.put(provider.authorizationCode(), info.getAuthorizationCode());
        parameters.put(provider.storeName(), info.getStoreName());
        parameters.put(provider.bin(), info.getBin());
        parameters.put(provider.city(), info.getCity());
        parameters.put(provider.address(), info.getAddress());
        parameters.put(provider.terminalId(), info.getTerminalId());
        parameters.put(provider.date(), info.getDate());
        parameters.put(provider.rrn(), info.getRrn());
        parameters.put(provider.orderNumber(), info.getOrderNumber());
        parameters.put(provider.cardMask(), info.getCardMask());
        parameters.put(provider.icc(), info.getIcc());
        parameters.put(provider.amount(), String.format("%s тнг", info.getAmount()));
        parameters.put(provider.status(), info.getStatus());
        parameters.put(provider.hostResponseCode(), info.getHostResponseCode());

        String baseDelimiter = ": ";

        int fallbackLength = parameters.entrySet().stream()
                .filter(it -> it.getValue() != null)
                .map(it -> it.getKey().length() + it.getValue().length() + baseDelimiter.length())
                .max(Comparator.comparingInt(it -> it))
                .orElse(50);

        int width = paperWidth == PAPER_WIDTH_UNKNOWN ? fallbackLength : paperWidth;
        return parameters.entrySet().stream()
                .filter(it -> it.getValue() != null)
                .flatMap(it -> {
                    String value = it.getValue();
                    int strLength = it.getKey().length() + value.length() + baseDelimiter.length();

                    int offset = width - value.length();

                    if (offset < 0) {
                        offset = 0;
                    }

                    return (strLength < width)
                            ? Stream.of(it.getKey() + baseDelimiter + String.join("", Collections.nCopies(width - strLength, " ")) + value)
                            : Stream.of(it.getKey() + baseDelimiter, String.join("", Collections.nCopies(offset, " ")) + value);
                })
                .collect(Collectors.joining("\n"));
    }

}
