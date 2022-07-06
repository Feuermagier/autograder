package de.firemage.codelinter.web.result.transfer;

import de.firemage.codelinter.core.GlobalProblem;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;

public class GlobalTransferProblem implements TransferProblem {
    private final GlobalProblem problem;

    public GlobalTransferProblem(GlobalProblem problem) {
        this.problem = problem;
    }

    public String getDisplayPath() {
        return this.problem.getDisplayLocation();
    }

    public ProblemCategory getCategory() {
        return this.problem.getCategory();
    }

    public String getDescription() {
        return this.problem.getDescription();
    }

    public String getExplanation() {
        return this.problem.getExplanation();
    }

    public ProblemPriority getPriority() {
        return this.problem.getPriority();
    }
}
