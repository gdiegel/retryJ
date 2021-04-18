package io.github.gdiegel.exception;

public class RetriesExhaustedException extends RuntimeException {

    public RetriesExhaustedException(String message) {
        super(message);
    }

}
