package de.firemage.autograder.core;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiInCodeProblem extends InCodeProblem {

    public MultiInCodeProblem(
        InCodeProblem firstProblem,
        Collection<? extends InCodeProblem> otherProblems
    ) {
        super(
            firstProblem.getCheck(),
            firstProblem.getPosition(),
            makeExplanation(firstProblem, otherProblems),
            firstProblem.getProblemType()
        );
    }

    private static Translatable makeExplanation(InCodeProblem first, Collection<? extends InCodeProblem> problems) {
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
                        problems.stream().map(InCodeProblem::getPosition)
                    )
                )
            ).format(bundle);
        };
    }

    private static String displayLocations(Path firstFile, Stream<CodePosition> positions) {
        Map<Path, List<CodePosition>> positionsByFile = positions
            .collect(Collectors.groupingBy(CodePosition::file, LinkedHashMap::new, Collectors.toList()));

        boolean withoutFilename = positionsByFile.size() == 1 && positionsByFile.containsKey(firstFile);

        StringJoiner joiner = new StringJoiner(", ");
        // Format should look like this: File:(L1, L2, L3), File2:(L4, L5), File3:L5
        for (Map.Entry<Path, List<CodePosition>> entry : positionsByFile.entrySet()) {
            Path path = entry.getKey();
            List<CodePosition> filePositions = entry.getValue();

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

            joiner.add("%s:%s".formatted(FilenameUtils.getBaseName(path.getFileName().toString()), lines));
        }

        return joiner.toString();
    }
}
