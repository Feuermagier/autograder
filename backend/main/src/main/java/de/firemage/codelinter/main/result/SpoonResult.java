package de.firemage.codelinter.main.result;

import de.firemage.codelinter.linter.spoon.Problem;
import de.firemage.codelinter.main.result.transfer.TransferProblem;

import java.util.List;
import java.util.stream.Collectors;

public record SpoonResult(List<TransferProblem> problems) {
    public static SpoonResult fromProblems(List<Problem> problems) {
        return new SpoonResult(problems.stream().map(TransferProblem::convertProblem).collect(Collectors.toList()));
    }
}
