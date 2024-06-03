package common.util;

import common.config.CustomerDisplayConfig;
import common.config.LogConfig;
import common.config.PluginConfig;
import common.config.UIConfig;
import common.exception.BaseError;
import common.exception.TerminalUnavailableError;
import common.service.TransactionAmountHandler;
import org.slf4j.Logger;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.spi.plugin.payment.InvalidPaymentException;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.UIForms;

import java.util.Collections;
import java.util.Optional;

import static common.config.CustomerDisplayConfig.getDisplay;

public class ErrorHandlingUtil {

    public static final String BASE_ERROR_MESSAGE = "Ошибка";

    private static final ThreadLocal<String> transactionIdFieldName = new ThreadLocal<String>() {{
        set("transaction.number.field.name");
    }};
    private static final ThreadLocal<String> terminalIdFieldName = new ThreadLocal<String>() {{
        set("terminal.number.field.name");
    }};

    public static void runWithHandling(Runnable action, PaymentCallback callback, String transactionIdFieldName, String terminalIdFieldName) {
        ErrorHandlingUtil.transactionIdFieldName.set(transactionIdFieldName);
        ErrorHandlingUtil.terminalIdFieldName.set(terminalIdFieldName);
        runWithHandling(action, callback);
    }

    public static void runWithHandling(Runnable action, PaymentCallback callback) {
        Logger logger = LogConfig.getLogger();
        UIForms ui = UIConfig.getUiForms();
        getDisplay().clear();
        try {
            action.run();
        } catch (TerminalUnavailableError e) {
//            Если в конфиге отключено ручное подтверждение или оно отключено для конкретно брошенного исключения - сразу завершаем
            if (!PluginConfig.getProperties().isManualConfirmation() || !e.isProcessManual()) {
                logger.error(e.getMessage(), e);
                ui.showErrorForm(e.getMessage(), callback::paymentNotCompleted);
            } else {
                ui.showDialogForm(
                        new DialogFormParameters("Проверьте статус оплаты в истории терминала. Оплата прошла?", "Да", "Нет"),
                        new DialogListener() {
                            @Override
                            public void eventCanceled() {
                                callback.paymentNotCompleted();
                            }

                            @Override
                            public void eventButton1pressed() {
                                handleYes(callback);
                            }

                            @Override
                            public void eventButton2pressed() {
                                callback.paymentNotCompleted();
                            }
                        }
                );
            }
        } catch (Exception e) {
            String message = (e instanceof BaseError) ? e.getMessage() : BASE_ERROR_MESSAGE;
            logger.error(message, e);
            getDisplay().clear();
            ui.showErrorForm(message, callback::paymentNotCompleted);
        }

    }


    private static void handleYes(PaymentCallback callback) {
        UIForms ui = UIConfig.getUiForms();
        String systemPassword = PluginConfig.getProperties().getSystemPassword();
        String passwordPattern = String.join("", Collections.nCopies(systemPassword.length(), "*"));
        ui.getInputForms().showPatternInputForm(
                "Введите пароль",
                "",
                "",
                "",
                passwordPattern,
                new InputListener() {
                    @Override
                    public void eventCanceled() {
                        callback.paymentNotCompleted();
                    }

                    @Override
                    public void eventInputComplete(String password) {


                        LogConfig.getLogger().trace("Got wrong password {} ", password);

                        if (systemPassword.equals(password)) {
                            handleRRN(callback);
                        } else {
                            ui.showDialogForm(
                                    new DialogFormParameters("Неправильный пароль. Попробовать еще раз?", "Да", "Нет"),
                                    new DialogListener() {
                                        @Override
                                        public void eventCanceled() {
                                            callback.paymentNotCompleted();
                                        }

                                        @Override
                                        public void eventButton1pressed() {
                                            handleYes(callback);
                                        }

                                        @Override
                                        public void eventButton2pressed() {
                                            callback.paymentNotCompleted();
                                        }
                                    }
                            );
                        }

                    }
                }
        );
    }

    private static void handleRRN(PaymentCallback callback) {

        UIForms ui = UIConfig.getUiForms();
        ui.getInputForms().showPatternInputForm(
                "№ заказа / RRN",
                "",
                "",
                "",
                "********************************",
                new InputListener() {
                    @Override
                    public void eventCanceled() {
                        callback.paymentNotCompleted();
                    }

                    @Override
                    public void eventInputComplete(String transactionNumber) {
                        handleTermId(transactionNumber, callback);
                    }
                }
        );
    }

    private static void handleTermId(String transactionNumber, PaymentCallback callback) {
        UIForms ui = UIConfig.getUiForms();

        ui.getInputForms().showPatternInputForm(
                "ID терминала",
                "",
                "",
                "",
                "**********",
                new InputListener() {
                    @Override
                    public void eventCanceled() {
                        callback.paymentNotCompleted();
                    }

                    @Override
                    public void eventInputComplete(String terminalId) {
                        Payment payment = new Payment();
                        Optional.ofNullable(TransactionAmountHandler.getTransactionAmount())
                                .ifPresent(payment::setSum);
                        payment.getData().put(transactionIdFieldName.get(), transactionNumber);
                        payment.getData().put(terminalIdFieldName.get(), terminalId);
                        try {
                            callback.paymentCompleted(payment);
                        } catch (InvalidPaymentException e) {
                            callback.paymentNotCompleted();
                        }
                    }
                }
        );
    }
}
