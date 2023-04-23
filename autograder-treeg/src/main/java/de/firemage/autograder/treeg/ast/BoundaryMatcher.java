package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record BoundaryMatcher(Type type) implements RegExNode {
    @Override
    public String toRegEx() {
        return switch (this.type) {

            case LINE_START -> "^";
            case LINE_END -> "$";
            case WORD_BOUNDARY -> "\\b";
            case NON_WORD_BOUNDARY -> "\\B";
            case INPUT_START -> "\\A";
            case MATCH_END -> "\\G";
            case INPUT_END_BEFORE_TERMINATOR -> "\\Z";
            case INPUT_END -> "\\z";
            case LINEBREAK -> "\\R";
        };
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine(this.type.toString());
    }

    public enum Type {
        LINE_START,
        LINE_END,
        WORD_BOUNDARY,
        NON_WORD_BOUNDARY,
        INPUT_START,
        MATCH_END,
        INPUT_END_BEFORE_TERMINATOR,
        INPUT_END,
        LINEBREAK
    }
}
