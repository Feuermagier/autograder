package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.ForLoopCanBeForeachRule;

public class ForToForEachCheck extends PMDCheck {
    private static final String DESCRIPTION = """
            for-loop should be a for-each-loop.
            """;

    public ForToForEachCheck() {
        super(DESCRIPTION, DESCRIPTION, new ForLoopCanBeForeachRule());
    }
}
