package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

import java.util.List;
import java.util.stream.Collectors;

public record Alternative(List<RegExNode> alternatives) implements RegExNode {
    @Override
    public String toRegEx() {
        return this.alternatives.stream().map(RegExNode::toRegEx).collect(Collectors.joining("|"));
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine("Alternatives");
        printer.indent();
        this.alternatives.forEach(a -> a.toTree(printer));
        printer.unindent();
    }
}
