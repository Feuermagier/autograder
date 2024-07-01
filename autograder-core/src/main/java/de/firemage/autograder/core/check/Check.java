package de.firemage.autograder.core.check;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.MultiInCodeProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface Check {
    LocalizedMessage getLinter();

    default List<Problem> merge(List<Problem> problems, int limit) {
        // use a dumb algorithm: keep the first limit - 1 problems, and merge the rest into a single problem
        if (problems.size() <= limit) {
            return problems;
        }

        List<Problem> result = new ArrayList<>(problems.subList(0, limit - 1));
        List<Problem> toMerge = problems.subList(limit - 1, problems.size());

        result.add(new MultiInCodeProblem(toMerge.get(0), toMerge.subList(1, toMerge.size())));

        return result;
    }

    default Optional<Integer> maximumProblems() {
        return Optional.empty();
    }
}
