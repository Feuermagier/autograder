package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class DoubleBraceInitializationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Double Brace Initialization should be avoided";

    public DoubleBraceInitializationCheck() {
        super(DESCRIPTION, createXPathRule("double brace initialization", "//AllocationExpression/ClassOrInterfaceBody[count(*)=1]/*/Initializer[@Static=false()]"));
    }
}
