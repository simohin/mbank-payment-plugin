package kz.kaspi.qr.plugin.integration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusCode {
    SUCCESS(0),
    NO_CERT(-10000),
    DEVICE_ID_NOT_FOUND(-1501),
    DEVICE_INACTIVE(-1502),
    DEVICE_ALREADY_REGISTERED(-1503),
    PURCHASE_NOT_FOUND(-1601),
    NO_TRADE_POINTS(-14000002),
    SERVICE_UNAVAILABLE(-999),
    PURCHASE_NOT_FOUND_BY_ID(-99000001),
    TRADE_POINT_NOT_FOUND(-99000002),
    INVALID_DEVICE_FOR_PURCHASE(-99000003),
    RETURN_SUM_IS_TOO_HIGH(-99000005),
    RETRY_RETURN(-99000006),
    INVALID_STATUS(-99000011),
    PARTIAL_REFUND_NOT_AVAILABLE(-99000020),
    TRADE_POINT_DISABLED(990000018),
    TRADE_POINT_QR_NOT_ACCEPTED(990000026),
    INVALID_AMOUNT(990000028),
    UNKNOWN(Integer.MIN_VALUE);

    private static final StatusCode[] VALUES = StatusCode.values();
    @JsonValue
    private final int code;

    @JsonCreator
    public static StatusCode of(String value) {
        return Arrays.stream(VALUES).filter(it -> it.getCode() == Integer.parseInt(value)).findFirst().orElse(UNKNOWN);
    }
}
