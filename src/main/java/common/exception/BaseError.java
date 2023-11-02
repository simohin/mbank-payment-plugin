package common.exception;

import java.util.Optional;

public class BaseError extends RuntimeException {

    public static final String BASE_ERROR = "Что-то пошло не так";

    public BaseError(String message) {
        super(Optional.ofNullable(message).orElse(BASE_ERROR), null);
    }

    public BaseError(String message, Throwable cause) {
        super(Optional.ofNullable(message).orElse(BASE_ERROR), cause);
    }

    public BaseError(Throwable cause) {
        super(BASE_ERROR, cause);
    }
}
