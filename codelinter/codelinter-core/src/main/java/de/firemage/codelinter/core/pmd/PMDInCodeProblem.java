package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.InCodeProblem;
import de.firemage.codelinter.core.PathUtil;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;
import java.io.File;

public class PMDInCodeProblem extends InCodeProblem {

    private final RuleViolation violation;
    private final File root;

    public PMDInCodeProblem(RuleViolation violation, File root) {
        super(PathUtil.getSanitizedPath(violation.getFilename(), root),
                violation.getBeginLine(),
                violation.getBeginColumn(),
                violation.getRule().getName(),
                ProblemCategory.OTHER,
                violation.getDescription(),
                mapPMDPriority(violation.getRule().getPriority()));
        this.violation = violation;
        this.root = root;
    }

    private static ProblemPriority mapPMDPriority(RulePriority priority) {
        return switch (priority) {
            case HIGH, MEDIUM_HIGH -> ProblemPriority.SEVERE;
            case MEDIUM, MEDIUM_LOW -> ProblemPriority.FIX_RECOMMENDED;
            case LOW -> ProblemPriority.INFO;
        };
    }

    @Override
    public String getDisplayLocation() {
        return PathUtil.getSanitizedPath(this.violation.getFilename(), this.root) + ":" + this.violation.getBeginLine();
    }
}
