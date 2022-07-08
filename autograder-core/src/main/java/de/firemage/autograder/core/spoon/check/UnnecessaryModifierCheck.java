package de.firemage.autograder.core.spoon.check;

import de.firemage.autograder.core.check.Check;
import spoon.reflect.declaration.CtMethod;

public class UnnecessaryModifierCheck extends AbstractLoggingProcessor<CtMethod<?>> {
    public static final String SYNCHRONIZED_DESCRIPTION = "Used 'synchronized'";
    public static final String STRICTFP_DESCRIPTION = "Used 'strictfp'";
    public static final String EXPLANATION = """
            You don't need to add synchronized or strictfp.
            These modifiers are not needed in the context of programming exercises.""";

    public UnnecessaryModifierCheck(Check check) {
        super(check);
    }

    @Override
    public void process(CtMethod<?> element) {
        if (element.isSynchronized()) {
            addProblem(element, SYNCHRONIZED_DESCRIPTION);
        }
        if (element.isStrictfp()) {
            addProblem(element, STRICTFP_DESCRIPTION);
        }
    }
}
