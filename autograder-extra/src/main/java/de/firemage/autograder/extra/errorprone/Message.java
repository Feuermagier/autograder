package de.firemage.autograder.extra.errorprone;

import de.firemage.autograder.core.CodePositionImpl;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.Check;

public final class Message {
    private final ProblemType problemType;
    private final LocalizedMessage message;

    private Message(ProblemType problemType, LocalizedMessage message) {
        this.problemType = problemType;
        this.message = message;
    }

    public static Message of(ProblemType problemType, LocalizedMessage message) {
        return new Message(problemType, message);
    }

    ProblemImpl toProblem(Check check, CodePositionImpl position) {
        return new ProblemImpl(check, position, this.message, this.problemType) {
        };
    }
}
