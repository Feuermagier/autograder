package de.firemage.autograder.treeg;

import java.util.ArrayDeque;
import java.util.Deque;

public class RegExLexer {
    private final String content;
    private int index;
    private final Deque<Integer> marks;

    public RegExLexer(String content) {
        this.content = content;
        this.index = 0;
        this.marks = new ArrayDeque<>();
    }

    public char consumeNext() throws InvalidRegExSyntaxException {
        char value = this.read(this.index);
        this.index++;
        return value;
    }

    public char peek() throws InvalidRegExSyntaxException {
        return this.peek(0);
    }

    public boolean peekInRange(char min, char max) throws InvalidRegExSyntaxException {
        char c = this.peek();
        return c >= min && c <= max;
    }

    public char peek(int offset) throws InvalidRegExSyntaxException {
        return this.read(this.index + offset);
    }

    public boolean hasNext() {
        return this.hasNext(1);
    }

    public boolean hasNext(int n) {
        return this.index <= this.length() - n;
    }

    public RegExElementType peekType() throws InvalidRegExSyntaxException {
        return this.peekType(0);
    }

    public RegExElementType peekType(int offset) throws InvalidRegExSyntaxException {
        if (!this.hasNext(offset + 1)) {
            return RegExElementType.EOF;
        }

        return switch (this.peek(offset)) {
            case '(' -> RegExElementType.GROUP_START;
            case ')' -> RegExElementType.GROUP_END;
            case '[' -> RegExElementType.CHARACTER_CLASS_START;
            case ']' -> RegExElementType.CHARACTER_CLASS_END;
            case '-' -> RegExElementType.RANGE;
            case '\\' -> RegExElementType.ESCAPE;
            case '^' -> RegExElementType.HAT;
            case '.' -> RegExElementType.DOT;
            case '$' -> RegExElementType.DOLLAR;
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> RegExElementType.NUMBER;
            case '|' -> RegExElementType.OR;
            case 0 -> RegExElementType.EOF;
            default -> RegExElementType.CHARACTER;
        };
    }

    public void expect(RegExElementType type) throws InvalidRegExSyntaxException {
        if (this.peekType() != type) {
            throw new InvalidRegExSyntaxException("Expected " + type + ", found '" + this.peek() + "'");
        }
        this.consumeNext();
    }

    public void expect(char c) throws InvalidRegExSyntaxException {
        if (this.peek() != c) {
            throw new InvalidRegExSyntaxException("Expected " + c + ", found '" + this.peek() + "'");
        }
        this.consumeNext();
    }

    public void mark() {
        this.marks.push(this.index);
    }

    public void backtrack() {
        this.index = this.marks.pop();
    }

    @Override
    public String toString() {
        StringBuilder underline = new StringBuilder();
        for (int i = 0; i < this.length() + 1; i++) {
            if (i == this.index) {
                underline.append("^");
            } else if (i == this.length()) {
                underline.append(" ");
            } else {
                underline.append(" ");
            }
        }
        return this.content + "\n" + underline;
    }

    private char read(int offset) throws InvalidRegExSyntaxException {
        if (offset < this.content.length()) {
            return this.content.charAt(offset);
        } else if (offset == this.content.length()) {
            return 0;
        } else {
            throw new InvalidRegExSyntaxException("End of input reached");
        }
    }

    public int length() {
        return this.content.length() + 1;
    }
}
