package common.service;

import org.slf4j.Logger;
import common.config.LogConfig;
import common.model.Operation;

public class TransactionOperationHandler {

    private static final Logger log = LogConfig.getLogger();
    private static final ThreadLocal<Operation> OPERATION = new ThreadLocal<>();

    public static void setOperation(Operation operation) {
        OPERATION.set(operation);
        log.debug("Transaction operation {}", OPERATION.get());
    }

    public static Operation getOperation() {
        return OPERATION.get();
    }
}
