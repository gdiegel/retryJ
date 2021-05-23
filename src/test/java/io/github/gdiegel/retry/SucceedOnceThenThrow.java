package io.github.gdiegel.retry;

public class SucceedOnceThenThrow {
    private boolean shouldThrow = false;

    public String invoke() {
        if (shouldThrow) {
            throw new RuntimeException("Pow!");
        } else {
            shouldThrow = true;
            return "Yippie!";
        }
    }
}
