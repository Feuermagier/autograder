package de.firemage.autograder.core.span;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class Formatter {
    private final Highlight highlight;
    private final boolean showLineNumbers;
    private final Optional<Integer> viewbox;
    private final String lineSeparator;

    public Formatter(String lineSeparator, Highlight highlight) {
        this.highlight = highlight;
        this.showLineNumbers = true;
        this.lineSeparator = lineSeparator;
        this.viewbox = Optional.of(1);
    }

    public Formatter(String lineSeparator, Highlight highlight, Integer viewbox) {
        this.highlight = highlight;
        this.showLineNumbers = true;
        this.lineSeparator = lineSeparator;
        this.viewbox = Optional.ofNullable(viewbox);
    }

    /**
     * Describes the number of lines that are relevant for the span.
     * <p>
     * It can be used to omit lines that are not relevant.
     * <p>
     * For example, a viewbox of 2 would result in the following output:
     * <p>
     * {@code
     * 1 |   fn main() {
     * |  ___________^
     * 2 | |     some code;
     * 3 | |     more code;
     * .. | |
     * 10 | |     more code;
     * 11 | |     more code
     * 12 | | }
     * | |_^
     * 13 |
     * 14 |   fn another_function {
     * }
     * <p>
     * Here the two lines before and after the span are shown.
     * So a viewbox of 0 will only show the lines of the span.
     *
     * @return the current value of the viewbox
     */
    public int viewbox() {
        return this.viewbox.orElse(Integer.MAX_VALUE);
    }

    private Span startView(Text text) {
        // for example could be 0
        int highlightStart = this.highlight.span().start().line();
        // when the viewbox is empty, the entire text is important
        if (this.viewbox.isEmpty()) {
            return text.span();
        }

        int viewbox = this.viewbox.get();

        Span span = new Span(
            new Position(Math.max(highlightStart - viewbox, 0), 0),
            new Position(Math.max(highlightStart + viewbox + 1, 0), 0)
        );

        return text.span().relativeIntersection(span);
    }

    private Span endView(Text text) {
        int highlightEnd = this.highlight.span().end().line();
        if (this.viewbox.isEmpty()) {
            return text.span();
        }

        int viewbox = this.viewbox.get();

        Span span = new Span(
            new Position(Math.max(highlightEnd - viewbox, this.startView(text).end().line()), 0),
            new Position(Math.max(highlightEnd + viewbox + 1, 0), 0)
        );

        return text.span().relativeIntersection(span);
    }

    private List<String> renderView(Text text, RenderState renderState) {
        List<String> result = new ArrayList<>();

        if (text.isEmpty()) {
            return result;
        }

        for (Line line : text.lines()) {
            result.add(this.renderSourceLine(
                line,
                renderState.lineNumberWidth(),
                renderState.offset(),
                renderState.isInMultiline()
            ));

            int finalOffset;
            if (this.highlight.span().isInline()) {
                finalOffset = renderState.offset();
            } else if (this.highlight.isMultilineStart(line.number())) {
                finalOffset = renderState.enterMultilineHighlight();
            } else if (this.highlight.isMultilineEnd(line.number())) {
                finalOffset = renderState.exitMultilineHighlight();
            } else {
                finalOffset = renderState.offset();
            }

            this.highlight.render(line.number()).ifPresent(highlight -> {
                result.add(this.renderLine(Optional.empty(), renderState.lineNumberWidth(), finalOffset, highlight));
            });
        }

        return result;
    }

    /**
     * Renders the given text of source code.
     *
     * @param text the text to highlight
     * @return the highlighted text
     */
    public String render(Text text) {
        StringJoiner result = new StringJoiner(this.lineSeparator);

        int offset = 0;
        if (this.highlight.isMultiline()) {
            offset = 2;
        }

        int lineNumberWidth = text.lineNumberWidth();
        RenderState renderState = new RenderState(offset, lineNumberWidth);

        Span startView = this.startView(text);
        Span endView = this.endView(text);

        for (String line : this.renderView(text.subText(startView), renderState)) {
            result.add(line);
        }

        if (startView.contains(endView)) {
            return result.toString();
        }

        if (!startView.isFollowedBy(endView)) {
            result.add(this.renderSkipLine(renderState.lineNumberWidth(), renderState.isInMultiline()));
        }

        for (String line : this.renderView(text.subText(endView), renderState)) {
            result.add(line);
        }

        return result.toString();
    }

    private String renderLine(Optional<String> lineNumber, int lineNumberWidth, int offset, String content) {
        if (this.showLineNumbers) {
            return "%s | %s%s".formatted(
                rightAlign(lineNumber.orElse(""), lineNumberWidth),
                " ".repeat(offset),
                content
            );
        } else {
            return "%s%s".formatted(
                " ".repeat(offset),
                content
            );
        }
    }

    private String renderSkipLine(int lineNumberWidth, boolean isInMultiline) {
        String skip = "..";
        String line = "";
        if (isInMultiline) {
            line = "|";
        }

        return this.renderLine(Optional.of(skip), lineNumberWidth, 0, line);
    }

    private String renderSourceLine(Line line, int lineNumberWidth, int offset, boolean isInMultiline) {
        String content = line.text();
        int finalOffset = offset;
        if (isInMultiline) {
            if (!line.isEmpty()) {
                content = " " + content;
            }

            content = "|" + content;
            finalOffset = 0;
        }

        return this.renderLine(Optional.of(String.valueOf(line.number() + 1)), lineNumberWidth, finalOffset, content);
    }

    private static String rightAlign(CharSequence text, int width) {
        int alignment = Math.min(width, text.length());

        return " ".repeat(width - alignment) + text;
    }
}
