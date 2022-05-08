package de.firemage.codelinter.core.check.debug;

import de.firemage.codelinter.core.spoon.SpoonCheck;
import de.firemage.codelinter.core.spoon.check.AssertProcessor;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import java.util.function.Supplier;

public class AssertCheck extends SpoonCheck {
    private static final String DESCRIPTION = """
            Assertions crash the entire program if they evaluate to false.
            Also they can be disabled, so never rely on them to e.g. check user input.
            They are great for testing purposes, but should not be part of your final solution.
            If you want to document an invariant, consider a comment.""";

    public AssertCheck() {
        super(DESCRIPTION);
    }

    @Override
    public Supplier<? extends CodeProcessor> getProcessor() {
        return () -> new AssertProcessor(this);
    }
}
