package de.firemage.autograder.core.dynamic;

import de.firemage.autograder.event.Event;
import java.util.List;

public record TestRunResult(List<Event> events, TestRunStatus status, String executorOutput) {

    public enum TestRunStatus {
        OK,
        ERROR_TEST_FAILURE,
    }
}
