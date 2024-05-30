package kz.kaspi.qr.plugin.integration;

import common.config.LogConfig;
import kz.kaspi.qr.plugin.integration.dto.PaymentDetails;
import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
import lombok.Data;
import org.slf4j.Logger;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class Context {

    private final String id;
    private final PaymentCallback callback;
    private PaymentStatus status;
    private String message;
    private AtomicBoolean lock = new AtomicBoolean(false);
    private Logger log = LogConfig.getLogger();
    private PaymentDetails details;

    public void lock() {
        lock.set(true);
        log.debug("Контекст заблокирован");
    }

    public void unlock() {
        lock.set(false);
        log.debug("Контекст разблокирован");
    }

    public boolean isLocked() {
        return lock.get();
    }
}
