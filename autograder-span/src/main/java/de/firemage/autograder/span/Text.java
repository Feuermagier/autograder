package de.firemage.autograder.span;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a text with a span.
 *
 * @param text the text
 * @param span the span of the text
 */
public record Text(String text, Span span) {
    public static Text fromString(int lineNumber, String text) {
        return new Text(text, Span.of(lineNumber, text));
    }

    public List<Line> lines() {
        int lineNumber = this.span.start().line();
        return Streams.zip(
            Stream.iterate(lineNumber, i -> i + 1),
            this.text.lines(),
            Line::new
        ).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    public Text subText(Span span) {
        return new Text(
            this.text.substring(span.start().offset(this.text), span.end().offset(this.text)),
            span
        );
    }

    public int lineNumberWidth() {
        int startLine = this.span.start().line() + 1;
        int endLine = this.span.end().line() + 1;

        // the width of the line number column, must be at least 2 for ..
        return Math.max(Math.max(stringLength(startLine), stringLength(endLine)), 2);
    }

    private static int stringLength(int number) {
        return String.valueOf(number).length();
    }
}
