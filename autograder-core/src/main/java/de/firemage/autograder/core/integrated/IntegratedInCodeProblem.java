package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodePositionImpl;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.SourceInfo;
import spoon.reflect.declaration.CtElement;

public class IntegratedInCodeProblem extends ProblemImpl {
    private final CtElement element;

    public IntegratedInCodeProblem(Check check, CtElement element, Translatable explanation,
                                   ProblemType problemType, SourceInfo sourceInfo) {
        super(check, mapSourceToCode(element, sourceInfo), explanation, problemType);

        this.element = element;
    }

    public static CodePositionImpl mapSourceToCode(CtElement element, SourceInfo sourceInfo) {
        return CodePositionImpl.fromSourcePosition(element.getPosition(), element, sourceInfo);
    }

    @Override
    public String toString() {
        return String.format(
            "IntegratedInCodeProblem { element: '%s', position: '%s' }", element, getPosition()
        );
    }
}
