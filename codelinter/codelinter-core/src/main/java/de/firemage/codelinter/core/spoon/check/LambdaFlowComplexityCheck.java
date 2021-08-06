package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.Query;

import java.util.List;

public class LambdaFlowComplexityCheck extends AbstractLoggingProcessor<CtLambda<?>> {
    public static final int COMPLEXITY_THRESHOLD = 6; //TODO Arbitrary number
    private static final String DESCRIPTION = "Overly complex lambda";
    private static final String EXPLANATION = """
            The lambda contains many flow breaking statements (return, throw, break, continue, yield).
            Therefore its control flow is not obvious.
            Consider creating helper methods""";

    public LambdaFlowComplexityCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtLambda<?> element) {
        List<CtElement> breakingElements = Query.getElements(element, e -> {
            if (e instanceof CtCFlowBreak flowBreak) {
                return !CheckUtil.isBreakInSwitch(flowBreak);
            } else {
                return false;
            }
        });
        if (breakingElements.size() > COMPLEXITY_THRESHOLD) {
            addProblem(element, DESCRIPTION, ProblemCategory.CONTROL_FLOW, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
        }
    }
}
