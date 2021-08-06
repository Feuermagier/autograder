package de.firemage.codelinter.web.result;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.web.result.transfer.TransferProblem;
import java.util.List;
import java.util.stream.Collectors;

public record SpotbugsResult(List<TransferProblem> problems) {
    public static SpotbugsResult fromProblems(List<Problem> problems) {
        return new SpotbugsResult(problems.stream().map(TransferProblem::convertProblem).collect(Collectors.toList()));
    }
}
