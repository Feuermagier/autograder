package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.PrimitiveWrapperInstantiationRule;

public class WrapperInstantiationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Don't instantiate primitive wrappers";

    public WrapperInstantiationCheck() {
        super(DESCRIPTION, new PrimitiveWrapperInstantiationRule());
    }
}
