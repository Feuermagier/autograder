package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;

import java.util.List;

public class MethodFlowComplexityCheck extends AbstractLoggingProcessor<CtMethod<?>> {
    public static final int COMPLEXITY_THRESHOLD = 6; //TODO Arbitrary number
    private static final String DESCRIPTION = "Overly complex method";
    private static final String EXPLANATION = """
            The method contains many flow breaking statements (return, throw, break, continue, yield).
            Therefore its control flow is not obvious.
            Consider splitting it into multiple simpler methods.""";

    public MethodFlowComplexityCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtMethod<?> element) {
        List<CtElement> breakingElements = Query.getElements(element, e -> {
            if (e instanceof CtCFlowBreak flowBreak) {
                return !CheckUtil.isBreakInSwitch(flowBreak) && !CheckUtil.isInLambda(flowBreak);
            } else {
                return false;
            }
        });
        if (breakingElements.size() > COMPLEXITY_THRESHOLD) {
            addProblem(element, DESCRIPTION, ProblemCategory.CONTROL_FLOW, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
        }
    }
}
