package io.github.gdiegel.retry.collaborators;

public class StringProvider {
    private static final String STRING = "abcdef";
    private int pos = 0;

    public char getNextChar() {
        return STRING.toCharArray()[pos++];
    }
}
