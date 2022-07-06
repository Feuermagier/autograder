package de.firemage.codelinter.core.check.general;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class DoubleBraceInitializationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Double Brace Initialization should be avoided";

    public DoubleBraceInitializationCheck() {
        super(DESCRIPTION, createXPathRule("double brace initialization", "Don't use the obscure 'double brace initialization' syntax", "//AllocationExpression/ClassOrInterfaceBody[count(*)=1]/*/Initializer[@Static=false()]"));
    }
}
