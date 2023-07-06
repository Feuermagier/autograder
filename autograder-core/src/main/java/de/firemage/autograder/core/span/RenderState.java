package de.firemage.autograder.core.span;

final class RenderState {
    private boolean isInMultiline;
    private final int lineNumberWidth;
    private int offset;

    RenderState(int offset, int lineNumberWidth) {
        this.offset = offset;
        this.lineNumberWidth = lineNumberWidth;
        this.isInMultiline = false;
    }

    /**
     * Updates the state to for a new multiline highlight.
     *
     * @return the offset for the current line
     */
    int enterMultilineHighlight() {
        this.isInMultiline = true;
        this.offset = Math.max(this.offset - 2, 0);
        return this.offset;
    }

    /**
     * Updates the state after finishing a multiline highlight.
     *
     * @return the offset for the current line
     */
    int exitMultilineHighlight() {
        this.isInMultiline = false;
        this.offset += 2;
        return this.offset - 2;
    }

    boolean isInMultiline() {
        return this.isInMultiline;
    }

    int lineNumberWidth() {
        return this.lineNumberWidth;
    }

    int offset() {
        return this.offset;
    }
}
