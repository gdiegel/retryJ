package io.github.gdiegel.exception;

public class RetryException extends RuntimeException {

    public RetryException(Exception cause) {
        super(cause);
    }

    public RetryException(String msg, Exception cause) {
        super(msg + ": " + cause.getMessage(), cause);
    }

}

