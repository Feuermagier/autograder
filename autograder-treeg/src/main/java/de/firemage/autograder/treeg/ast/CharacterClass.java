package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

import java.util.List;
import java.util.stream.Collectors;

public record CharacterClass(boolean negated, List<CharacterClassEntry> ranges) implements RegExNode {
    @Override
    public String toRegEx() {
        return "[" + (this.negated ? "^" : "") + this.ranges.stream().map(CharacterClassEntry::toRegEx).collect(Collectors.joining()) + "]";
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine("Character Class" + (this.negated ? " (negated)" : ""));
        printer.indent();
        this.ranges.forEach(r -> r.toTree(printer));
        printer.unindent();
    }
}
