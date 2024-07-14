package de.firemage.autograder.extra.pmd;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.file.SourceInfo;
import net.sourceforge.pmd.RuleViolation;

import java.nio.file.Path;

public class PMDInCodeProblem extends Problem {

    public PMDInCodeProblem(PMDCheck check, RuleViolation violation, SourceInfo sourceInfo) {
        super(check,
            new CodePosition(
                sourceInfo,
                sourceInfo.getCompilationUnit(Path.of(violation.getFileId().getOriginalPath())).path(),
                violation.getBeginLine(),
                violation.getBeginLine(),
                violation.getBeginColumn(),
                violation.getBeginColumn()),
            check.getExplanation() != null ? check.getExplanation().apply(violation) : new LocalizedMessage(violation.getDescription()),
            check.getProblemType());
    }

    @Override
    public String toString() {
        return "PMDInCodeProblem[check=%s, position=%s, explanation=%s, problemType=%s]".formatted(
            this.getCheck(),
            this.getPosition(),
            this.getExplanation(),
            this.getProblemType()
        );
    }
}
