package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedFormalParameterRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedLocalVariableRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedPrivateFieldRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedPrivateMethodRule;
import java.util.List;

public class UnusedCodeElementCheck extends PMDCheck {
    private static final String DESCRIPTION = "Unused code element (local variable / parameter / private attribute / private method)";

    public UnusedCodeElementCheck() {
        super(DESCRIPTION, "Not used", List.of(
                new UnusedLocalVariableRule(),
                new UnusedFormalParameterRule(),
                new UnusedPrivateFieldRule(),
                new UnusedPrivateMethodRule())
        );
    }
}
