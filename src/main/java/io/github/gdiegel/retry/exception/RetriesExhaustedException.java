package io.github.gdiegel.retry.exception;

/**
 * A {@link RetriesExhaustedException}
 *
 * @author Gabriel Diegel
 */
public class RetriesExhaustedException extends RuntimeException {

    public RetriesExhaustedException(String message) {
        super(message);
    }

}
