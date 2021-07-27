package de.firemage.codelinter.main.result.transfer;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;

public class InCodeTransferProblem implements TransferProblem {
    private final InCodeProblem problem;

    public InCodeTransferProblem(InCodeProblem problem) {
        this.problem = problem;
    }

    public String getQualifiedClass() {
        return this.problem.getQualifiedClassName();
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
}
