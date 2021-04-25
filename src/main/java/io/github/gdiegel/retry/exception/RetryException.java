package io.github.gdiegel.retry.exception;

public class RetryException extends RuntimeException {

    public RetryException(Exception cause) {
        super(cause);
    }

}

