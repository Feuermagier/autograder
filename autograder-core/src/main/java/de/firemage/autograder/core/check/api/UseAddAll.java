package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL})
public class UseAddAll extends IntegratedCheck {

    private void checkAddAll(CtForEach ctFor) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1) {
            return;
        }

        if (statements.get(0) instanceof CtInvocation<?> ctInvocation
            && SpoonUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Collection.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "add", Object.class)
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

            String addAllArg = ctFor.getExpression().prettyprint();
            if (ctFor.getExpression().getType().isArray()) {
                addAllArg = "Arrays.asList(%s)".formatted(addAllArg);
            }


            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s.addAll(%s)".formatted(
                            ctInvocation.getTarget().prettyprint(),
                            addAllArg
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL
            );
        }
    }


    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtForEach>() {
            @Override
            public void process(CtForEach ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                checkAddAll(ctFor);
            }
        });
    }
}
