package de.firemage.autograder.core.span;

import de.firemage.autograder.core.CodePosition;

import java.util.Optional;

public record Highlight(Span span, Optional<String> label, Style style) {
    // TODO: support multiline labels (required for correctly displaying things)

    public static Highlight from(CodePosition codePosition, String label) {
        return new Highlight(
            Span.of(codePosition),
            Optional.ofNullable(label),
            Style.ERROR
        );
    }

    public boolean isMultilineStart(int lineNumber) {
        return !this.span.isInline() && this.span.start().line() == lineNumber;
    }

    public boolean isMultilineEnd(int lineNumber) {
        return !this.span.isInline() && this.span.end().line() == lineNumber;
    }

    public Optional<String> render(int lineNumber) {
        if (!this.span.includesLine(lineNumber)) {
            return Optional.empty();
        }

        if (this.span.isInline()) {
            return Optional.of(this.renderInline(this.span.start().column(), this.span.end().column()));
        } else if (this.isMultilineStart(lineNumber)) {
            // first line of multiline span, looks like this:
            //   fn main() {
            //  ________^
            // (note the two spaces before the code)
            // span starts at line 0, column 7
            String highlight = " %s%s".formatted(
                "_".repeat(this.span.start().column()),
                this.style.marker()
            );

            return Optional.of(highlight);
        } else if (this.isMultilineEnd(lineNumber)) {
            // last line of multiline span, looks like this:
            //   fn main() {
            //  ________^
            // |
            // | }
            // |_^
            // (note the two spaces before the code, this is the offset)
            // span ends at line 2, column 1
            String highlight = "|%s%s%s".formatted(
                "_".repeat(this.span.end().column()),
                this.style.marker(),
                this.label.map(label -> " " + label).orElse("")
            );

            return Optional.of(highlight);
        } else {
            return Optional.empty();
        }
    }

    private String renderInline(int start, int end) {
        // for example:
        // fn main() {
        //    ^^^^ this is the span
        //
        // would have the span:
        // start: Position(line: 0, column: 3), end: Position(line: 0, column: 7)
        //
        // so this method gets: lineNumber = 0, start = 3, end = 7
        // There are 'start' space before the span

        return "%s%s%s".formatted(
            " ".repeat(start),
            this.style.marker().repeat(end - start),
            // prepend a space so ^^^label becomes ^^^ label
            this.label.map(label -> " " + label).orElse("")
        );
    }

    public boolean isMultiline() {
        return !this.span.isInline();
    }
}
