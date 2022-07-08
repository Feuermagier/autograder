package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.ForLoopCanBeForeachRule;

public class ForToForEachCheck extends PMDCheck {
    private static final String DESCRIPTION = """
            for-loop should be a for-each-loop.
            """;

    public ForToForEachCheck() {
        super(DESCRIPTION, DESCRIPTION, new ForLoopCanBeForeachRule());
    }
}
