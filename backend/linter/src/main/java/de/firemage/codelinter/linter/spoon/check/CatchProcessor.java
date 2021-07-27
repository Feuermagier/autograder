package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.code.CtCatch;
import spoon.reflect.declaration.CtClass;

public class CatchProcessor extends AbstractLoggingProcessor<CtCatch> {
    private static final String DESCRIPTION = "Empty catch block";
    private static final String EXPLANATION = """
            There is no reason to have an empty catch block in your program. 
            If you are sure that the caught exception will never be thrown, throw an IllegalStateException in the catch block.""";

    public CatchProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtCatch element) {
        if (element.getBody().getStatements().isEmpty()) {
            addProblem(new InCodeProblem(element.getParent(CtClass.class), element.getPosition(), DESCRIPTION, ProblemCategory.EXCEPTION, EXPLANATION));
        }
    }
}
