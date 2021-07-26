package de.firemage.codelinter.linter.spoon.processor;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.support.SpoonClassNotFoundException;

import java.lang.reflect.Method;

public class IllegalExitProcessor extends LoggingProcessor<CtInvocation<Void>> {

    public IllegalExitProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtInvocation<Void> element) {
        try {
            Method method = element.getExecutable().getActualMethod();
            if (method != null && method.equals(System.class.getMethod("exit", int.class))) {
                addProblem(new InCodeProblem(element.getParent(CtClass.class), element.getPosition(), "Used System.exit()", ProblemCategory.OTHER));
            } else if (method != null && method.equals(Runtime.class.getMethod("exit", int.class))) {
                addProblem(new InCodeProblem(element.getParent(CtClass.class), element.getPosition(), "Used Runtime.exit()", ProblemCategory.OTHER));
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (SpoonClassNotFoundException e) {
            // Workaround; see https://github.com/INRIA/spoon/issues/4066
        }
    }
}
