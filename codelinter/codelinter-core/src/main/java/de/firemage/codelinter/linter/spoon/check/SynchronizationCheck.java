package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;

// TODO
public class SynchronizationCheck implements Check {

    private final ProblemLogger logger;

    public SynchronizationCheck(ProblemLogger logger) {
        this.logger = logger;
    }

    @Override
    public void check(CtModel model, Factory factory) {

    }
}
