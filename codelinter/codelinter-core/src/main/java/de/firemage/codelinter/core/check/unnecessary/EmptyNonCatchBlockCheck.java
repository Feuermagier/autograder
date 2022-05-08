package de.firemage.codelinter.core.check.unnecessary;

import de.firemage.codelinter.core.pmd.PMDCheck;
import java.util.List;

public class EmptyNonCatchBlockCheck extends PMDCheck {
    private static final String DESCRIPTION = "Empty block (if / else / for / while / switch / try)";

    public EmptyNonCatchBlockCheck() {
        super(DESCRIPTION, List.of(
                createXPathRule("empty if", "//IfStatement/Statement[EmptyStatement or Block[not(*)]]"),
                createXPathRule("empty while", "//WhileStatement/Statement[Block[not(*)] or EmptyStatement]"),
                createXPathRule("empty try", "//TryStatement[not(ResourceSpecification)]/Block[1][not(*)]"),
                createXPathRule("empty finally", "//FinallyStatement[not(Block/BlockStatement)]"),
                createXPathRule("empty switch", "//SwitchStatement[count(*) = 1]")
        ));
    }
}
