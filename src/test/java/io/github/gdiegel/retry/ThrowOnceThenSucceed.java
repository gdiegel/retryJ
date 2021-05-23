package io.github.gdiegel.retry;

public class ThrowOnceThenSucceed {
    private boolean thrown = false;

    String invoke() {
        if (thrown) {
            return "Yippie!";
        } else {
            thrown = true;
            throw new RuntimeException("Pow!");
        }
    }
}
