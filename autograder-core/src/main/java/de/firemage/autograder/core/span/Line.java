package de.firemage.autograder.core.span;

public record Line(int number, String text) {
    public int length() {
        return this.text.length();
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }
}
