package common.service;

import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import common.model.Operation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static common.service.TransactionAmountHandler.setTransactionAmount;
import static common.service.TransactionOperationHandler.setOperation;
import static common.service.TransactionStartHandler.setTransactionStart;

public class RefundService extends SharedResourcesService {

    public void process(RefundRequest request) {
        if (configProperties.isShowSumEnterForm()) {
            uiService.showSumEnterForm(request, amount -> doProcess(request, amount), configProperties.isActiveSumEnterFormRefund());
            return;
        }

        doProcess(request, request.getRefundReceipt().getSurchargeSum());
    }

    private void doProcess(RefundRequest request, BigDecimal amount) {
        setTransactionStart();
        setTransactionAmount(amount);
        setOperation(Operation.REFUND);
        bankIntegrationService.process(request, amount.setScale(0, RoundingMode.FLOOR).intValue());
    }

}
