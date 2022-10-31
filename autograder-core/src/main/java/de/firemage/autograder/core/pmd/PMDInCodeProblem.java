package de.firemage.autograder.core.pmd;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.InCodeProblem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import net.sourceforge.pmd.RuleViolation;

import java.nio.file.Path;

public class PMDInCodeProblem extends InCodeProblem {

    public PMDInCodeProblem(PMDCheck check, RuleViolation violation, Path root) {
        super(check,
            new CodePosition(
                root.relativize(Path.of(violation.getFilename())),
                violation.getBeginLine(),
                violation.getEndLine(),
                violation.getBeginColumn(),
                violation.getEndColumn()),
            check.getExplanation() != null ? check.getExplanation() : new LocalizedMessage(violation.getDescription()),
            check.getProblemType());
    }
}
