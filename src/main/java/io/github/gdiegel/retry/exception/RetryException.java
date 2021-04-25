package io.github.gdiegel.retry.exception;

/**
 * A {@link RetryException}
 *
 * @author Gabriel Diegel
 */
public class RetryException extends RuntimeException {

    public RetryException(Exception cause) {
        super(cause);
    }

}

