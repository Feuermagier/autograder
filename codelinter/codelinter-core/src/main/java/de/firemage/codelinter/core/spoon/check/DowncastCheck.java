package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtExpression;

public class DowncastCheck extends AbstractLoggingProcessor<CtExpression<?>> {
    public static final String DESCRIPTION = "Cast outside of equals";
    public static final String EXPLANATION = """
            Remove the cast if you can do so without triggering a compiler error.
            Rethink your design if you can't remove the cast.
            Downcasts (even if properly checked) generally indicate bad OOP design.
            The only exception is the required cast in 'equals'.
            """;

    public DowncastCheck(Check check) {
        super(check);
    }

    @Override
    public void process(CtExpression<?> element) {
        if (!CheckUtil.isInEquals(element)
                && !element.getTypeCasts().stream().allMatch(c -> c.unbox().isPrimitive())) {
            addProblem(element, EXPLANATION);
        }
    }
}
