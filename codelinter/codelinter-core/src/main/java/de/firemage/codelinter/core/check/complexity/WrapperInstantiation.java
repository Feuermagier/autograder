package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.PrimitiveWrapperInstantiationRule;

public class WrapperInstantiation extends PMDCheck {
    private static final String DESCRIPTION = "Don't instantiate primitive wrappers";

    public WrapperInstantiation() {
        super(DESCRIPTION, new PrimitiveWrapperInstantiationRule());
    }
}
