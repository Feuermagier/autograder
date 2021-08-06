package de.firemage.codelinter.main.result.transfer;

import de.firemage.codelinter.linter.InCodeProblem;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;

public class InCodeTransferProblem implements TransferProblem {
    private final InCodeProblem problem;

    public InCodeTransferProblem(InCodeProblem problem) {
        this.problem = problem;
    }

    public String getDisplayPath() {
        return this.problem.getDisplayLocation();
    }

    public String getFilePath() {
        return this.problem.getFilePath();
    }

    public int getLine() {
        return this.problem.getLine();
    }

    public int getColumn() {
        return this.problem.getColumn();
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
