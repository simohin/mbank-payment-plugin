package kz.kaspi.qr.plugin.integration;

import kz.kaspi.qr.plugin.integration.dto.PaymentStatus;
import lombok.Data;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class Context {

    private final String id;
    private final PaymentCallback callback;
    private PaymentStatus status;
    private AtomicBoolean lock = new AtomicBoolean(false);

    public void lock() {
        lock.set(true);
    }

    public void unlock() {
        lock.set(false);
    }

    public boolean isLocked() {
        return lock.get();
    }
}
