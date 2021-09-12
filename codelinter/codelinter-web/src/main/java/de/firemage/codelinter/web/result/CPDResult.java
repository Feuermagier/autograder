package de.firemage.codelinter.web.result;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.web.result.transfer.TransferProblem;
import java.util.List;
import java.util.stream.Collectors;

public record CPDResult(List<TransferProblem> problems) {
    public static CPDResult fromProblems(List<Problem> problems) {
        return new CPDResult(problems.stream().map(TransferProblem::convertProblem).collect(Collectors.toList()));
    }
}
