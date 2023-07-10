package de.firemage.autograder.span;

/**
 * Represents a position in a file.
 *
 * @param line the line number, starting at 0
 * @param column the column, starting at 0
 */
public record Position(int line, int column) implements Comparable<Position> {
    public static Position fromOffset(int offset, CharSequence string) {
        int currentLine = 0;
        int currentLineStart = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\n') {
                currentLine += 1;
                currentLineStart = i + 1;
            }

            if (i == offset) {
                return new Position(currentLine, i - currentLineStart);
            }
        }

        throw new IllegalArgumentException("%d is not in text".formatted(offset));
    }

    public int offset(CharSequence string) {
        int currentLine = 0;
        int currentLineStart = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\n') {
                currentLine += 1;
                currentLineStart = i + 1;
            }

            if (currentLine == this.line) {
                int result = currentLineStart + this.column;
                // NOTE: this might allow invalid positions
                if (result > string.length()) {
                    throw new IllegalArgumentException("%s is not in string".formatted(this));
                }

                return result;
            }
        }

        throw new IllegalArgumentException("%s is not in string".formatted(this));
    }

    @Override
    public int compareTo(Position other) {
        int lineDiff = Integer.compare(this.line(), other.line());
        if (lineDiff != 0) {
            return lineDiff;
        }

        return Integer.compare(this.column(), other.column());
    }

    @Override
    public String toString() {
        return "Position(L%d:%d)".formatted(this.line(), this.column());
    }
}
