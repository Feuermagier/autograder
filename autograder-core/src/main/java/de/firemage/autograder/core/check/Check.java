package de.firemage.autograder.core.check;

import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.MultiInCodeProblem;
import de.firemage.autograder.core.ProblemImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Check {
    Translatable getLinter();

    default List<ProblemImpl> merge(List<ProblemImpl> problems, int limit) {
        // use a dumb algorithm: keep the first limit - 1 problems, and merge the rest into a single problem
        if (problems.size() <= limit) {
            return problems;
        }

        List<ProblemImpl> result = new ArrayList<>(problems.subList(0, limit - 1));
        List<ProblemImpl> toMerge = problems.subList(limit - 1, problems.size());

        result.add(new MultiInCodeProblem(toMerge.get(0), toMerge.subList(1, toMerge.size())));

        return result;
    }

    default Optional<Integer> maximumProblems() {
        return Optional.empty();
    }
}
