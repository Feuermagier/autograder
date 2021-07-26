package de.firemage.codelinter.main.result.transfer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.Problem;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface TransferProblem {
    static TransferProblem convertProblem(Problem problem) {
        if (problem instanceof InCodeProblem inCodeProblem) {
            return new InCodeTransferProblem(inCodeProblem);
        } else {
            throw new IllegalStateException("Unknown problem type");
        }
    }
}
