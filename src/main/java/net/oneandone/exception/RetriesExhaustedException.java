package net.oneandone.exception;

/**
 * Created by gdiegel on 6/23/16.
 */
public class RetriesExhaustedException extends RuntimeException {

    public RetriesExhaustedException() {
    }

    public RetriesExhaustedException(String message) {
        super(message);
    }

    public RetriesExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetriesExhaustedException(Throwable cause) {
        super(cause);
    }

    public RetriesExhaustedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
