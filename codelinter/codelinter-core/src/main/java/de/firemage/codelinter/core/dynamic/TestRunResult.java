package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.event.Event;
import java.util.List;

public record TestRunResult(List<Event> events, TestRunStatus status, String executorOutput) {

    public enum TestRunStatus {
        OK,
        ERROR_TEST_FAILURE,
    }
}
