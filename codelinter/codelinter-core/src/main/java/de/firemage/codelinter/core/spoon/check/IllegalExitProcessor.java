package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtInvocation;
import spoon.support.SpoonClassNotFoundException;

import java.lang.reflect.Method;

public class IllegalExitProcessor extends AbstractLoggingProcessor<CtInvocation<Void>> {
    private static final String DESCRIPTION_SYSTEM = "Used System.exit()";
    private static final String DESCRIPTION_RUNTIME = "Used Runtime.exit()";
    //TODO Was ist mit Artemis?
    private static final String EXPLANATION = """
            System.exit() and Runtime.exit() may interfere with the Praktomat.
            They may indicate bad design, because they create additional control paths through your program.
            Consider using exceptions.""";

    public IllegalExitProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtInvocation<Void> element) {
        try {
            Method method = element.getExecutable().getActualMethod();
            if (method != null && method.equals(System.class.getMethod("exit", int.class))) {
                addProblem(element, DESCRIPTION_SYSTEM, ProblemCategory.OTHER, EXPLANATION, ProblemPriority.SEVERE);
            } else if (method != null && method.equals(Runtime.class.getMethod("exit", int.class))) {
                addProblem(element, DESCRIPTION_RUNTIME, ProblemCategory.OTHER, EXPLANATION, ProblemPriority.SEVERE);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (SpoonClassNotFoundException e) {
            // Workaround; see https://github.com/INRIA/spoon/issues/4066
        }
    }
}
