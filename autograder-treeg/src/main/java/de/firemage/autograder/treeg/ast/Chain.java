package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

import java.util.List;
import java.util.stream.Collectors;

public record Chain(List<RegExNode> children) implements RegExNode {
    @Override
    public String toRegEx() {
        return this.children.stream().map(RegExNode::toRegEx).collect(Collectors.joining());
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine("Chain");
        printer.indent();
        this.children.forEach(n -> n.toTree(printer));
        printer.unindent();
    }
}
