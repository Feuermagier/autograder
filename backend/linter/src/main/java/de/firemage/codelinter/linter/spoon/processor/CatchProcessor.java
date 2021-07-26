package de.firemage.codelinter.linter.spoon.processor;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.code.CtCatch;
import spoon.reflect.declaration.CtClass;

public class CatchProcessor extends LoggingProcessor<CtCatch> {

    public CatchProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtCatch element) {
        if (element.getBody().getStatements().isEmpty()) {
            addProblem(new InCodeProblem(element.getParent(CtClass.class), element.getPosition(), "Emtpy catch block", ProblemCategory.EXCEPTION));
        }
    }
}
