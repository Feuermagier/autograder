package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;

import java.util.List;
import java.util.Map;


@ExecutableCheck(reportedProblems = { ProblemType.SIMPLIFY_ARRAYS_FILL })
public class SimplifyArraysFill extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (ctInvocation.isImplicit() || !ctInvocation.getPosition().isValidPosition()) {
                    return;
                }

                if (!(ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess)
                    || !SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), java.util.Arrays.class)
                    || !ctInvocation.getExecutable().getSimpleName().equals("fill")
                    || ctInvocation.getArguments().size() != 4) {
                    return;
                }

                List<CtExpression<?>> args = ctInvocation.getArguments();
                if (SpoonUtil.resolveConstant(args.get(1)) instanceof CtLiteral<?> ctLiteral
                    && ctLiteral.getValue() instanceof Integer number
                    && number == 0
                    && args.get(2) instanceof CtFieldAccess<?> ctFieldAccess
                    && args.get(0).equals(ctFieldAccess.getTarget())
                    && ctFieldAccess.getVariable().getSimpleName().equals("length")) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of(
                                "suggestion", "Arrays.fill(%s, %s)".formatted(args.get(0), args.get(3))
                            )
                        ),
                        ProblemType.SIMPLIFY_ARRAYS_FILL
                    );
                }
            }
        });
    }
}
