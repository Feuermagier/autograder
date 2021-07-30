package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.code.CtExpression;

public class DowncastCheck extends AbstractLoggingProcessor<CtExpression<?>> {
    public static final String DESCRIPTION = "Cast outside of equals";
    public static final String EXPLANATION = """
            Remove the cast if you can do so without triggering a compiler error.
            Rethink your design if you can't remove the cast.
            Downcasts (even if properly checked) generally indicate bad OOP design.
            The only exception is the required cast in 'equals'.
            """;

    public DowncastCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtExpression<?> element) {
        if (!CheckUtil.isInEquals(element)
                && !element.getTypeCasts().stream().allMatch(c -> c.unbox().isPrimitive())) {
            addProblem(new SpoonInCodeProblem(element, DESCRIPTION, ProblemCategory.OOP, EXPLANATION));
        }
    }
}
