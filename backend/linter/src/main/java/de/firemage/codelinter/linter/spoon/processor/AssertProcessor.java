package de.firemage.codelinter.linter.spoon.processor;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.code.CtAssert;
import spoon.reflect.declaration.CtClass;

public class AssertProcessor extends LoggingProcessor<CtAssert<?>> {
    public AssertProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtAssert<?> element) {
        addProblem(new InCodeProblem(element.getParent(CtClass.class), element.getPosition(), "Used 'assert'", ProblemCategory.JAVA_FEATURE));
    }
}
