package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.InCodeProblem;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import spoon.reflect.declaration.CtElement;

public class SpoonInCodeProblem extends InCodeProblem {
    public SpoonInCodeProblem(CtElement element, String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        super(FormatUtil.formatPath(element),
                element.getPosition().getFile().getPath(),
                element.getPosition().getLine(),
                element.getPosition().getColumn(),
                description,
                category,
                explanation,
                priority);
    }
}
