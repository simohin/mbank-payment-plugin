package common.service;

import common.model.Operation;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static common.service.TransactionAmountHandler.setTransactionAmount;
import static common.service.TransactionOperationHandler.setOperation;
import static common.service.TransactionStartHandler.setTransactionStart;

public class PaymentService extends SharedResourcesService {

    public void process(PaymentRequest request) {
        if (configProperties.isShowSumEnterForm()) {
            uiService.showSumEnterForm(request, amount -> doProcess(request, amount), configProperties.isActiveSumEnterFormPayment());
            return;
        }

        doProcess(request, request.getReceipt().getSurchargeSum());
    }

    private void doProcess(PaymentRequest request, BigDecimal amount) {
        uiService.showSpinner("Выполняется запрос на сервер");
        setTransactionStart();
        setTransactionAmount(amount);
        setOperation(Operation.PAYMENT);
        bankIntegrationService.process(request, amount.setScale(0, RoundingMode.FLOOR).intValue());
    }

}
