package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record Lookaround(RegExNode child, Type type) implements RegExNode {
    @Override
    public String toRegEx() {
        return "(" + switch (this.type) {
            case LOOKBEHIND -> "?<=";
            case NEGATIVE_LOOKBEHIND -> "?<!";
            case LOOKAHEAD -> "?=";
            case NEGATIVE_LOOKAHEAD -> "?!";
        } + this.child.toRegEx() + ")";
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine(this.type.toString());
        printer.indent();
        this.child.toTree(printer);
        printer.unindent();
    }

    public enum Type {
        LOOKBEHIND,
        NEGATIVE_LOOKBEHIND,
        LOOKAHEAD,
        NEGATIVE_LOOKAHEAD,
    }
}
