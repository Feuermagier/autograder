package de.firemage.autograder.treeg;

import de.firemage.autograder.treeg.ast.RegExNode;

public record RegularExpression(RegExNode root) {
    public String toRegEx() {
        return this.root.toRegEx();
    }

    public String toTree() {
        TreePrinter printer = new TreePrinter();
        this.root.toTree(printer);
        return printer.toString();
    }
}
