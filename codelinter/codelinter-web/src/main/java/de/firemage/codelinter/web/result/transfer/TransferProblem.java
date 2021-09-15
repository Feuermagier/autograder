package de.firemage.codelinter.web.result.transfer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.firemage.codelinter.core.GlobalProblem;
import de.firemage.codelinter.core.InCodeProblem;
import de.firemage.codelinter.core.Problem;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface TransferProblem {
    static TransferProblem convertProblem(Problem problem) {
        if (problem instanceof InCodeProblem inCodeProblem) {
            return new InCodeTransferProblem(inCodeProblem);
        } else if (problem instanceof GlobalProblem globalProblem) {
            return new GlobalTransferProblem(globalProblem);
        } else {
            throw new IllegalStateException("Unknown problem type");
        }
    }
}
