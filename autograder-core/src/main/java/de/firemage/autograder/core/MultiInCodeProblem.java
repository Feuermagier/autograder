package de.firemage.autograder.core;

import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.file.SourcePath;
import org.apache.commons.io.FilenameUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiInCodeProblem extends ProblemImpl {

    public MultiInCodeProblem(
        ProblemImpl firstProblem,
        Collection<? extends ProblemImpl> otherProblems
    ) {
        super(
            firstProblem.getCheck(),
            firstProblem.getPosition(),
            makeExplanation(firstProblem, otherProblems),
            firstProblem.getProblemType()
        );
    }

    private static Translatable makeExplanation(ProblemImpl first, Collection<? extends ProblemImpl> problems) {
        return bundle -> {
            String message = first.getExplanation().format(bundle);
            if (!message.endsWith(".")) {
                message += ".";
            }

            return new LocalizedMessage(
                "merged-problems",
                Map.of(
                    "message", message,
                    "locations", displayLocations(
                        first.getPosition().file(),
                        problems.stream().map(ProblemImpl::getPosition)
                    )
                )
            ).format(bundle);
        };
    }

    private static String displayLocations(SourcePath firstFile, Stream<CodePositionImpl> positions) {
        Map<SourcePath, List<CodePositionImpl>> positionsByFile = positions
            .collect(Collectors.groupingBy(CodePositionImpl::file, LinkedHashMap::new, Collectors.toList()));

        boolean withoutFilename = positionsByFile.size() == 1 && positionsByFile.containsKey(firstFile);

        StringJoiner joiner = new StringJoiner(", ");
        // Format should look like this: File:(L1, L2, L3), File2:(L4, L5), File3:L5
        for (Map.Entry<SourcePath, List<CodePositionImpl>> entry : positionsByFile.entrySet()) {
            SourcePath path = entry.getKey();
            List<CodePositionImpl> filePositions = entry.getValue();

            String lines = filePositions.stream()
                .map(position -> "L%d".formatted(position.startLine()))
                .collect(Collectors.joining(", "));

            if (filePositions.size() > 1 && !withoutFilename) {
                lines = "(%s)".formatted(lines);
            }

            if (withoutFilename) {
                joiner.add(lines);
                continue;
            }

            joiner.add("%s:%s".formatted(FilenameUtils.getBaseName(path.getName()), lines));
        }

        return joiner.toString();
    }
}
