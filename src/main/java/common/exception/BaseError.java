package common.exception;

import java.util.Optional;

public class BaseError extends RuntimeException {

    public static final String DEFAULT_ERROR_MESSAGE = "Қате\nТағы байқап көріңіз\n\nОшибка\nПопробуйте еще раз";

    public BaseError(String message) {
        super(Optional.ofNullable(message).orElse(DEFAULT_ERROR_MESSAGE), null);
    }

    public BaseError(String message, Throwable cause) {
        super(Optional.ofNullable(message).orElse(DEFAULT_ERROR_MESSAGE), cause);
    }

    public BaseError(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
