package kz.kaspi.qr.plugin.integration;

import common.config.LogConfig;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatusData;
import kz.kaspi.qr.plugin.integration.dto.ReturnData;
import lombok.Data;
import org.slf4j.Logger;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class Context {

    private final String id;
    private final PaymentCallback callback;
    private String message;
    private AtomicBoolean lock = new AtomicBoolean(false);
    private Logger log = LogConfig.getLogger();
    private PaymentDetails details;
    private PaymentStatusData statusData;
    private Type type = Type.PAYMENT;
    private Set<PaymentStatus> processedStatuses = new HashSet<>();
    private ReturnData returnData;

    public void lock() {
        lock.set(true);
        log.debug("Контекст заблокирован");
    }

    public void unlock() {
        lock.set(false);
        log.debug("Контекст разблокирован");
    }

    public void addProcessedStatus(PaymentStatus status) {
        processedStatuses.add(status);
    }

    public PaymentStatus getStatus() {
        return Optional.ofNullable(statusData)
                .map(PaymentStatusData::getStatus)
                .orElse(PaymentStatus.UNKNOWN);
    }

    public boolean checkIsStatusProcessed(PaymentStatus status) {
        return processedStatuses.contains(status);
    }

    public boolean isLocked() {
        return lock.get();
    }

    public enum Type {
        PAYMENT,
        RETURN
    }
}
