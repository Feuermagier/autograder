package de.firemage.codelinter.main.result;

import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.main.result.transfer.TransferProblem;
import java.util.List;
import java.util.stream.Collectors;

public record PMDResult(List<TransferProblem> problems) {
    public static PMDResult fromProblems(List<Problem> problems) {
        return new PMDResult(problems.stream().map(TransferProblem::convertProblem).collect(Collectors.toList()));
    }
}
