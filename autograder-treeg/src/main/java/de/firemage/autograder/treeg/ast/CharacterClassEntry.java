package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public sealed interface CharacterClassEntry permits RegExCharacter, CharacterRange {
    String toRegEx();
    void toTree(TreePrinter printer);
}
