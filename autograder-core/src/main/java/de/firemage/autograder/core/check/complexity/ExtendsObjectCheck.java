package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class ExtendsObjectCheck extends PMDCheck {
    private static final String DESCRIPTION = "Explicitly extending Object is unnecessary";

    public ExtendsObjectCheck() {
        super(DESCRIPTION, createXPathRule("extends object", "Unnecessary 'extends Object'",
                "//ExtendsList/ClassOrInterfaceType[@Image='Object' or @Image='java.lang.Object']"),
            ProblemType.EXPLICITLY_EXTENDS_OBJECT);
    }
}
