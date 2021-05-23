package io.github.gdiegel.retry.collaborators;

public class ThrowImmediately {

    public String invoke() {
        throw new RuntimeException("Pow!");
    }
}
