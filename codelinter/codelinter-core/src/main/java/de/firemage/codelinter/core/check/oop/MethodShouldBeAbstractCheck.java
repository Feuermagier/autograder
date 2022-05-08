package de.firemage.codelinter.core.check.oop;

import de.firemage.codelinter.core.spoon.SpoonCheck;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import de.firemage.codelinter.core.spoon.check.MethodShouldBeAbstractProcessor;
import java.util.function.Supplier;

public class MethodShouldBeAbstractCheck extends SpoonCheck {
    private static final String DESCRIPTION = "Empty methods in abstract classes should be abstract";

    public MethodShouldBeAbstractCheck() {
        super(DESCRIPTION);
    }

    @Override
    public Supplier<? extends CodeProcessor> getProcessor() {
        return () -> new MethodShouldBeAbstractProcessor(this);
    }
}
