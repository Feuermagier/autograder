package de.firemage.autograder.extra.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.extra.errorprone.ErrorProneCheck;
import de.firemage.autograder.extra.errorprone.ErrorProneDiagnostic;
import de.firemage.autograder.extra.errorprone.ErrorProneLint;
import de.firemage.autograder.extra.errorprone.Message;

import java.util.Map;
import java.util.function.Function;

@ExecutableCheck(reportedProblems = {ProblemType.DOUBLE_BRACE_INITIALIZATION})
public class DoubleBraceInitializationCheck implements ErrorProneCheck {
    @Override
    public Map<ErrorProneLint, Function<ErrorProneDiagnostic, Message>> subscribedLints() {
        return Map.ofEntries(
            Map.entry(
                // Prefer collection factory methods or builders to the double-brace initialization pattern.
                //
                // https://errorprone.info/bugpattern/DoubleBraceInitialization
                ErrorProneLint.fromString("DoubleBraceInitialization"),
                diagnostic -> Message.of(
                    ProblemType.DOUBLE_BRACE_INITIALIZATION,
                    new LocalizedMessage("double-brace-init")
                )
            )
        );
    }
}
