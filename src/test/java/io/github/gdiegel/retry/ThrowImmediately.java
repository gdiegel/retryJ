package io.github.gdiegel.retry;

public class ThrowImmediately {

    public String invoke() {
        throw new RuntimeException("Pow!");
    }
}
