package de.firemage.codelinter.linter.pmd;

import de.firemage.codelinter.linter.InCodeProblem;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public class PMDInCodeProblem extends InCodeProblem {
    public PMDInCodeProblem(RuleViolation violation) {
        super(violation.getFilename(),
                violation.getFilename(),
                violation.getBeginLine(),
                violation.getBeginColumn(),
                violation.getRule().getName(),
                ProblemCategory.OTHER,
                violation.getDescription(),
                mapPMDPriority(violation.getRule().getPriority()));
    }

    private static ProblemPriority mapPMDPriority(RulePriority priority) {
        return switch (priority) {
            case HIGH, MEDIUM_HIGH -> ProblemPriority.SEVERE;
            case MEDIUM, MEDIUM_LOW -> ProblemPriority.FIX_RECOMMENDED;
            case LOW -> ProblemPriority.INFO;
        };
    }
}
