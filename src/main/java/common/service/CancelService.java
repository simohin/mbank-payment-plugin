package common.service;

import common.model.Operation;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static common.service.TransactionStartHandler.setTransactionStart;

public class CancelService extends SharedResourcesService {

    public void process(CancelRequest request) {
        BigDecimal amount = request.getPayment().getSum();
        setTransactionStart();
        TransactionAmountHandler.setTransactionAmount(amount);
        TransactionOperationHandler.setOperation(Operation.CANCEL);
        bankIntegrationService.process(request, amount.setScale(0, RoundingMode.FLOOR).intValue());
    }
}
