package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record Quantifier(RegExNode child, Type type, int min, int max) implements RegExNode {
    @Override
    public String toRegEx() {
        return this.child.toRegEx() + switch (this.type) {
            case AT_MOST_ONCE -> "?";
            case ANY -> "*";
            case AT_LEAST_ONCE -> "+";
            case TIMES -> "{" + this.min + "}";
            case OPEN_RANGE -> "{" + this.min + ",}";
            case RANGE -> "{" + this.min + "," + this.max + "}";
        };
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine(this.type + " [" + this.min + ", " + this.max + "]");
        printer.indent();
        this.child.toTree(printer);
        printer.unindent();
    }

    public enum Type {
        AT_MOST_ONCE,
        ANY,
        AT_LEAST_ONCE,
        TIMES,
        OPEN_RANGE,
        RANGE
    }
}
