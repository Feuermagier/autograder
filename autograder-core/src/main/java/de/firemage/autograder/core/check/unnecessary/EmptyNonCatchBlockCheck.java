package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

import java.util.List;

public class EmptyNonCatchBlockCheck extends PMDCheck {
    public EmptyNonCatchBlockCheck() {
        super(new LocalizedMessage("empty-block-desc"), List.of(
            createXPathRule("empty if", "empty-block-exp-if",
                "//IfStatement/Statement[EmptyStatement or Block[not(*)]]"),
            createXPathRule("empty while", "empty-block-exp-while",
                "//WhileStatement/Statement[Block[not(*)] or EmptyStatement]"),
            createXPathRule("empty try", "empty-block-exp-try",
                "//TryStatement[not(ResourceSpecification)]/Block[1][not(*)]"),
            createXPathRule("empty finally", "empty-block-exp-finally",
                "//FinallyStatement[not(Block/BlockStatement)]"),
            createXPathRule("empty switch", "empty-block-exp-switch", "//SwitchStatement[count(*) = 1]")
        ), ProblemType.EMPTY_BLOCK);
    }
}
