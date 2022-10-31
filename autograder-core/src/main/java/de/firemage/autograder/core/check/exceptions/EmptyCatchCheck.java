package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class EmptyCatchCheck extends PMDCheck {
    public EmptyCatchCheck() {
        super(new LocalizedMessage("empty-catch-desc"),
            createXPathRule("empty catch",
                "empty-catch-exp",
                "//CatchStatement[not(Block/BlockStatement)]"),
            ProblemType.EMPTY_CATCH);
    }
}
