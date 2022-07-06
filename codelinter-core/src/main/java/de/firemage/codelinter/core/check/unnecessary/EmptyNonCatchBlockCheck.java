package de.firemage.codelinter.core.check.unnecessary;

import de.firemage.codelinter.core.pmd.PMDCheck;
import java.util.List;

public class EmptyNonCatchBlockCheck extends PMDCheck {
    private static final String DESCRIPTION = "Empty block (if / else / for / while / switch / try)";

    public EmptyNonCatchBlockCheck() {
        super(DESCRIPTION, List.of(
                createXPathRule("empty if", "Empty if/else block", "//IfStatement/Statement[EmptyStatement or Block[not(*)]]"),
                createXPathRule("empty while", "Empty while loop", "//WhileStatement/Statement[Block[not(*)] or EmptyStatement]"),
                createXPathRule("empty try", "Empty try block", "//TryStatement[not(ResourceSpecification)]/Block[1][not(*)]"),
                createXPathRule("empty finally", "Empty finally block", "//FinallyStatement[not(Block/BlockStatement)]"),
                createXPathRule("empty switch", "Empty switch block", "//SwitchStatement[count(*) = 1]")
        ));
    }
}
