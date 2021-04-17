package io.github.gdiegel.exception;

/**
 * Created by gdiegel on 6/23/16.
 */
public class RetryException extends RuntimeException {

    public RetryException() {
        super();
    }

    public RetryException(Exception cause) {
        super(cause);
    }

    public RetryException(String msg) {
        super(msg);
    }

    public RetryException(String msg, Exception cause) {
        super(msg + ": " + cause.getMessage(), cause);
    }

}

