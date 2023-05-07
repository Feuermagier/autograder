package de.firemage.autograder.core.errorprone;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.InCodeProblem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
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

    Problem toProblem(Check check, CodePosition position) {
        return new InCodeProblem(check, position, this.message, this.problemType) {
        };
    }
}
