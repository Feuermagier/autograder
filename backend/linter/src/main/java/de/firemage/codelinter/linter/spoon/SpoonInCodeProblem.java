package de.firemage.codelinter.linter.spoon;

import spoon.reflect.declaration.CtElement;

public class SpoonInCodeProblem extends InCodeProblem {
    public SpoonInCodeProblem(CtElement element, String description, ProblemCategory category, String explanation) {
        super(FormatUtil.formatPath(element),
                element.getPosition().getFile().getPath(),
                element.getPosition().getLine(),
                element.getPosition().getColumn(),
                description,
                category,
                explanation);
    }
}
