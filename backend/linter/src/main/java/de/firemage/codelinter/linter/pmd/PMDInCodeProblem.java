package de.firemage.codelinter.linter.pmd;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import net.sourceforge.pmd.RuleViolation;

public class PMDInCodeProblem extends InCodeProblem {
    public PMDInCodeProblem(RuleViolation violation) {
        super(violation.getFilename(),
                violation.getFilename(),
                violation.getBeginLine(),
                violation.getBeginColumn(),
                violation.getRule().getName(),
                ProblemCategory.OTHER,
                violation.getDescription());
    }
}
