package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;

import java.util.List;

@ExecutableCheck(reportedProblems = {ProblemType.FOR_WITH_MULTIPLE_VARIABLES})
public class ForLoopVariableCheck extends PMDCheck {
    public ForLoopVariableCheck() {
        super(new LocalizedMessage("for-loop-var-desc"), List.of(
            createXPathRule("multi variable for", "for-loop-var-exp",
                "//ForInit/LocalVariableDeclaration[count(VariableDeclarator) > 1]")
        ), ProblemType.FOR_WITH_MULTIPLE_VARIABLES);
    }
}
