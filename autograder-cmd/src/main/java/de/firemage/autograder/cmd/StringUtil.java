package de.firemage.autograder.cmd;

public final class StringUtil {
    private StringUtil() {

    }

    public static String center(String s, int length, String padding) {
        if (padding.length() != 1) {
            throw new IllegalArgumentException("padding must be a string of length 1");
        }
        return length > s.length()
                ? padding.repeat((length - s.length()) / 2) + s + padding.repeat((length - s.length() + 1) / 2)
                : s;
    }
}
