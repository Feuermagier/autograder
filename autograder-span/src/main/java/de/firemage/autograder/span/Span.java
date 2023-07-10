package de.firemage.autograder.span;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Optional;

/**
 * A span of text in a file.
 *
 * @param start the start of the span (inclusive)
 * @param end the end of the span (exclusive)
 */
public record Span(Position start, Position end) {
    // TODO: consider adding a last position to the span for marking the end of a line


    /**
     * Creates a span which covers the entire string.
     *
     * @param lineNumber the line number where the string starts (0-indexed)
     * @param string the string to span
     * @return a span which covers the entire string
     */
    public static Span of(int lineNumber, String string) {
        Position start = new Position(lineNumber, 0);
        String[] lines = string.split("\\R", -1);
        Position end = new Position(lineNumber + lines.length - 1, lines[lines.length - 1].length());

        return new Span(start, end);
    }

    public boolean isInline() {
        return this.start.line() == this.end.line();
    }

    public boolean includesLine(int lineNumber) {
        if (this.end.column() == 0) {
            return this.start.line() <= lineNumber && lineNumber < this.end.line();
        }

        return this.start.line() <= lineNumber && lineNumber <= this.end.line();
    }

    public boolean isEmpty() {
        // start >= end means that the span is empty
        return this.start.compareTo(this.end) >= 0;
    }

    /**
     * Creates an empty span contained in this span.
     *
     * @return the empty span
     */
    private Span empty() {
        return new Span(this.start, this.start);
    }

    public boolean isFollowedBy(Span other) {
        return this.end().equals(other.start());
    }

    public boolean contains(Span span) {
        return this.intersection(span).map(span::equals).orElse(false);
    }

    /**
     * Makes the given span relative to this span.
     *
     * @param span the span to relativize, must be contained in this span
     * @return the relative span
     */
    public Span relativize(Span span) {
        if (!this.contains(span)) {
            throw new IllegalArgumentException("Span must be contained in this span");
        }

        Position start = new Position(span.start().line() - this.start().line(), span.start().column());
        Position end = new Position(span.end().line() - this.start().line(), span.end().column());

        return new Span(start, end);
    }

    public Optional<Span> intersection(Span other) {
        if (other.isEmpty()) {
            return Optional.of(other);
        }

        Position start = ObjectUtils.max(this.start(), other.start());
        Position end = ObjectUtils.min(this.end(), other.end());

        Span result = new Span(start, end);
        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    public Span relativeIntersection(Span other) {
        return this.relativize(this.intersection(other).orElseGet(this::empty));
    }
}
