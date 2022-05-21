package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class ExtendsObjectCheck extends PMDCheck {
    private static final String DESCRIPTION = "Explicitly extending Object is unnecessary";

    public ExtendsObjectCheck() {
        super(DESCRIPTION, createXPathRule("extends object", "Unnecessary 'extends Object'", "//ExtendsList/ClassOrInterfaceType[@Image='Object' or @Image='java.lang.Object']"));
    }
}
