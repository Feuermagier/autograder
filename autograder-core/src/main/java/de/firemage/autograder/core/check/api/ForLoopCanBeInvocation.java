package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.FOR_LOOP_CAN_BE_INVOCATION })
public class ForLoopCanBeInvocation extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtForEach>() {
            @Override
            public void process(CtForEach ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctFor.getBody());
                if (statements.size() != 1) {
                    return;
                }

                if (statements.get(0) instanceof CtInvocation<?> ctInvocation
                    && TypeUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Collection.class)
                    && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "add", Object.class)
                    && ctInvocation.getExecutable().getParameters().size() == 1
                    // ensure that the add argument simply accesses the for variable:
                    // for (T t : array) {
                    //     collection.add(t);
                    // }
                    && ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead
                    && ctVariableRead.getVariable().equals(ctFor.getVariable().getReference())) {

                    // allow explicit casting
                    if (!ctInvocation.getArguments().get(0).getTypeCasts().isEmpty()) {
                        return;
                    }

                    // handle edge case where the variable is implicitly cast in the invocation (Character in List, but char in iterable)
                    List<CtTypeReference<?>> actualTypeArguments = ctInvocation.getTarget().getType().getActualTypeArguments();
                    if (!actualTypeArguments.isEmpty() && !ctFor.getVariable().getType().equals(actualTypeArguments.get(0))) {
                        return;
                    }

                    String addAllArg = ctFor.getExpression().toString();
                    if (ctFor.getExpression().getType().isArray()) {
                        addAllArg = "Arrays.asList(%s)".formatted(addAllArg);
                    }


                    addLocalProblem(
                        ctFor,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of(
                                "suggestion", "%s.addAll(%s)".formatted(
                                    ctInvocation.getTarget(),
                                    addAllArg
                                )
                            )
                        ),
                        ProblemType.FOR_LOOP_CAN_BE_INVOCATION
                    );
                }
            }
        });
    }
}
