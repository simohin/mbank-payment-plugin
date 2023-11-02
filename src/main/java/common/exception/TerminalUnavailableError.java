package common.exception;

public class TerminalUnavailableError extends BaseError {

    public static final String ERROR_MESSAGE = "Терминал недоступен. Обратитесь к Системному администратору";
    private boolean processManual = true;


    public TerminalUnavailableError() {
        super(ERROR_MESSAGE);
    }

    public TerminalUnavailableError(Throwable cause) {
        super(ERROR_MESSAGE, cause);
    }

    public boolean isProcessManual() {
        return processManual;
    }

    public void setProcessManual(boolean processManual) {
        this.processManual = processManual;
    }

}
