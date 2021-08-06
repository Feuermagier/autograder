package de.firemage.codelinter.core.spotbugs;

import de.firemage.codelinter.core.InCodeProblem;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.annotations.Confidence;

public class SpotbugsInCodeProblem extends InCodeProblem {
    private final BugInstance bug;

    public SpotbugsInCodeProblem(BugInstance bug) {
        super(bug.getPrimaryClass().getSourceFileName(),
                bug.getPrimarySourceLineAnnotation().getStartLine(),
                -1,
                bug.getAbridgedMessage(),
                ProblemCategory.OTHER,
                bug.getBugPattern().getDetailText(),
                mapPriority(Confidence.getConfidence(bug.getPriority()))
        );
        this.bug = bug;
    }

    private static ProblemPriority mapPriority(Confidence confidence) {
        return switch (confidence) {
            case HIGH -> ProblemPriority.SEVERE;
            case MEDIUM -> ProblemPriority.FIX_RECOMMENDED;
            case LOW -> ProblemPriority.INFO;
            case IGNORE -> throw new IllegalArgumentException("confidence must not be 'ignore'");
        };
    }

    @Override
    public String getDisplayLocation() {
        return this.bug.getPrimaryClass().getSlashedClassName();
    }
}
