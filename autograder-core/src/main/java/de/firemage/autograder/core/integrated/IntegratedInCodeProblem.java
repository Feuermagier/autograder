package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.Translatable;
import de.firemage.autograder.core.check.Check;
import spoon.reflect.declaration.CtElement;

import java.nio.file.Path;

public class IntegratedInCodeProblem extends ProblemImpl {
    private final CtElement element;

    public IntegratedInCodeProblem(Check check, CtElement element, Translatable explanation,
                                   ProblemType problemType, Path root) {
        super(check, mapSourceToCode(element, root), explanation, problemType);

        this.element = element;
    }

    public static CodePosition mapSourceToCode(CtElement element, Path root) {
        return CodePosition.fromSourcePosition(element.getPosition(), element, root);
    }

    @Override
    public String toString() {
        return String.format(
            "IntegratedInCodeProblem { element: '%s', position: '%s' }", element, getPosition()
        );
    }
}
