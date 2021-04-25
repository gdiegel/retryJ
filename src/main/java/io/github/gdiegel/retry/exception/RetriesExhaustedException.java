package io.github.gdiegel.retry.exception;

public class RetriesExhaustedException extends RuntimeException {

    public RetriesExhaustedException(String message) {
        super(message);
    }

}
