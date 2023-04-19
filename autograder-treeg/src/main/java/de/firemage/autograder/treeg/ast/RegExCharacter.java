package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record RegExCharacter(char content, boolean escaped) implements RegExNode, CharacterClassEntry {
    @Override
    public String toRegEx() {
        return (this.escaped ? "\\" : "") + this.content;
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine(this.content + (this.escaped ? " (escaped)" : ""));
    }
}
