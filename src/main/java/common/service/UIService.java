package common.service;

import common.config.ResBundleConfig;
import common.util.ErrorHandlingUtil;
import ru.crystals.pos.api.ui.listener.ConfirmListener;
import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.api.ui.listener.SumToPayFormListener;
import ru.crystals.pos.spi.ResBundle;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.UIForms;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static common.config.UIConfig.getUiForms;

public class UIService {

    private final UIForms ui = getUiForms();
    private static final ResBundle resBundle = ResBundleConfig.getResBundle();
    private static final String ENTER_SUM_TO_PAY = resBundle.getString("enter.sum.to.pay");
    public static final String CAPTION = resBundle.getString("plugin.name");

    public void showSpinner(String key) {
        ui.showSpinnerForm(resBundle.getString(key));
    }

    public void showSumEnterForm(PaymentRequest request, Consumer<BigDecimal> amountConsumer, boolean activeSumEnter) {
        showSumEnterForm(request.getReceipt(), request.getReceipt().getSurchargeSum(), amountConsumer, request.getPaymentCallback(), activeSumEnter);
    }

    public void showSumEnterForm(RefundRequest request, Consumer<BigDecimal> amountConsumer, boolean activeSumEnter) {
        showSumEnterForm(request.getRefundReceipt(), request.getSumToRefund(), amountConsumer, request.getPaymentCallback(), activeSumEnter);
    }

    public void showDialog(String title, final Runnable onYes, final Runnable onNo) {
        ui.showDialogForm(
                new DialogFormParameters(title, "Да", "Нет"),
                new DialogListener() {
                    @Override
                    public void eventButton1pressed() {
                        onYes.run();
                    }

                    @Override
                    public void eventButton2pressed() {
                        onNo.run();
                    }

                    @Override
                    public void eventCanceled() {
                        onNo.run();
                    }
                }
        );
    }

    public void showError(String title, ConfirmListener listener) {
        ui.showErrorForm(title, listener);
    }

    private void showSumEnterForm(Receipt receipt, BigDecimal amount, Consumer<BigDecimal> amountConsumer, PaymentCallback callback, boolean activeSumEnter) {

        SumToPayFormParameters parameters = buildFormParameters(receipt, amount, activeSumEnter);
        SumToPayFormListener listener = buildSumToPayFormListener(amountConsumer, callback);

        ui.getPaymentForms().showSumToPayForm(parameters, listener);
    }

    private SumToPayFormListener buildSumToPayFormListener(Consumer<BigDecimal> amountConsumer, PaymentCallback callback) {
        return new SumToPayFormListener() {
            @Override
            public void eventCanceled() {
                callback.paymentNotCompleted();
            }

            @Override
            public void eventSumEntered(BigDecimal amount) {
                ErrorHandlingUtil.runWithHandling(() -> amountConsumer.accept(amount), callback);
            }
        };
    }

    private SumToPayFormParameters buildFormParameters(Receipt receipt, BigDecimal amount, boolean activeSumEnter) {
        SumToPayFormParameters parameters = new SumToPayFormParameters(CAPTION, receipt);
        parameters.setInputHint(resBundle.getString(ENTER_SUM_TO_PAY));
        parameters.setDefaultSum(amount);
        if (activeSumEnter) {
            parameters.setMinSum(amount);
        }
        return parameters;
    }
}
