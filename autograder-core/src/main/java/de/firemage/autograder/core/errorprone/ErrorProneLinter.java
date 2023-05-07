package de.firemage.autograder.core.errorprone;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.SourceInfo;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.UploadedFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ErrorProneLinter {
    public List<Problem> lint(UploadedFile file, Iterable<? extends ErrorProneCheck> checks) throws IOException {
        Map<ErrorProneLint, Function<ErrorProneDiagnostic, Message>> lintsForChecks = new HashMap<>();
        Map<ErrorProneLint, Check> checksForLints = new HashMap<>();

        for (ErrorProneCheck check : checks) {
            for (var entry : check.subscribedLints().entrySet()) {
                ErrorProneLint lint = entry.getKey();

                if (lintsForChecks.containsKey(lint)) {
                    throw new IllegalStateException("Lint " + lint + " is used by multiple checks");
                }

                checksForLints.put(lint, check);
                lintsForChecks.put(lint, entry.getValue());
            }
        }

        List<ErrorProneLint> lints = new ArrayList<>(lintsForChecks.keySet());

        SourceInfo code = file.getSource();

        ErrorProneCompiler compiler = new ErrorProneCompiler(
            code.getVersion(),
            lints
        );

        List<ErrorProneDiagnostic> diagnostics = compiler.compile(code);

        Map<ErrorProneLint, List<ErrorProneDiagnostic>> diagnosticMapping = new HashMap<>();

        for (ErrorProneDiagnostic diagnostic : diagnostics) {
            ErrorProneLint lint = diagnostic.lint();

            diagnosticMapping.computeIfAbsent(lint, key -> new ArrayList<>());
            diagnosticMapping.get(lint).add(diagnostic);
        }

        List<Problem> result = new ArrayList<>();

        for (var entry : lintsForChecks.entrySet()) {
            ErrorProneLint lint = entry.getKey();
            Function<ErrorProneDiagnostic, Message> getMessage = entry.getValue();

            List<ErrorProneDiagnostic> diagnosticList = diagnosticMapping.getOrDefault(lint, List.of());

            for (ErrorProneDiagnostic diagnostic : diagnosticList) {
                Message message = getMessage.apply(diagnostic);
                result.add(message.toProblem(
                    checksForLints.get(lint),
                    diagnostic.position())
                );
            }
        }

        return result;
    }
}
