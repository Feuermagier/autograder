package de.firemage.autograder.extra.errorprone;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.check.Check;

import java.util.Map;
import java.util.function.Function;

/**
 * This is the interface for all checks that are executed by error-prone.
 */
@FunctionalInterface
public interface ErrorProneCheck extends Check {
    /**
     * Returns a map of lints that this check is interested in and a function that converts the
     * resulting diagnostic to a message.
     * @return the map, must not be null and no two checks should subscribe to the same lint
     */
    Map<ErrorProneLint, Function<ErrorProneDiagnostic, Message>> subscribedLints();

    @Override
    default LocalizedMessage getLinter() {
        return new LocalizedMessage("linter-error-prone");
    }
}
