package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class ExtendsObjectCheck extends PMDCheck {
    private static final LocalizedMessage DESCRIPTION = new LocalizedMessage("extends-object-desc");

    public ExtendsObjectCheck() {
        super(DESCRIPTION, createXPathRule("extends object", "extends-object-exp",
                "//ExtendsList/ClassOrInterfaceType[@Image='Object' or @Image='java.lang.Object']"),
            ProblemType.EXPLICITLY_EXTENDS_OBJECT);
    }
}
