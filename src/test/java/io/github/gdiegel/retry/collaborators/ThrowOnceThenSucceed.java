package io.github.gdiegel.retry.collaborators;

public class ThrowOnceThenSucceed {
    private boolean thrown = false;

    public String invoke() {
        if (thrown) {
            return "Yippie!";
        } else {
            thrown = true;
            throw new RuntimeException("Pow!");
        }
    }
}
