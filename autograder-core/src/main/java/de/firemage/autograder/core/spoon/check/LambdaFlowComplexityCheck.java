package de.firemage.autograder.core.spoon.check;

import de.firemage.autograder.core.check.Check;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.Query;

import java.util.List;

public class LambdaFlowComplexityCheck extends AbstractLoggingProcessor<CtLambda<?>> {
    private static final String DESCRIPTION = "Overly complex lambda";
    private static final String EXPLANATION = """
            The lambda contains many flow breaking statements (return, throw, break, continue, yield).
            Therefore its control flow is not obvious.
            Consider creating helper methods""";

    private final int complexityThreshold;

    public LambdaFlowComplexityCheck(Check check, int complexityThreshold) {
        super(check);
        this.complexityThreshold = complexityThreshold;
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
        if (breakingElements.size() > this.complexityThreshold) {
            addProblem(element, EXPLANATION);
        }
    }
}
