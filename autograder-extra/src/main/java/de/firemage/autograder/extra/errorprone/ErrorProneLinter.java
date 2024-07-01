package de.firemage.autograder.extra.errorprone;

import de.firemage.autograder.core.CodeLinter;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ErrorProneLinter implements CodeLinter<ErrorProneCheck> {
    @Override
    public Class<ErrorProneCheck> supportedCheckType() {
        return ErrorProneCheck.class;
    }

    public List<Problem> lint(
        UploadedFile submission,
        TempLocation tempLocation,
        ClassLoader classLoader,
        List<ErrorProneCheck> checks,
        Consumer<? super LinterStatus> statusConsumer
    ) throws IOException {
        statusConsumer.accept(LinterStatus.RUNNING_ERROR_PRONE);
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

        SourceInfo code = submission.getSource();

        ErrorProneCompiler compiler = new ErrorProneCompiler(
            code.getVersion(),
            tempLocation,
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
