package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.COLLECTIONS_N_COPIES })
public class CollectionsNCopies extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());

                if (statements.size() != 1
                    || forLoopRange == null
                    || !(statements.get(0) instanceof CtInvocation<?> ctInvocation)
                    || !(ctInvocation.getExecutable().getSimpleName().equals("add"))
                    || ctInvocation.getArguments().size() != 1
                    || !(ctInvocation.getTarget() instanceof CtVariableRead<?> ctVariableRead)
                    || !SpoonUtil.isSubtypeOf(ctVariableRead.getType(), java.util.Collection.class)) {
                    return;
                }

                // return if the for loop uses the loop variable (would not be a simple repetition)
                if (!ctFor.getBody().getElements(new VariableAccessFilter<>(forLoopRange.loopVariable())).isEmpty()) {
                    return;
                }

                CtExpression<?> rhs = ctInvocation.getArguments().get(0);
                if (!SpoonUtil.isImmutable(rhs.getType())) {
                    return;
                }

                addLocalProblem(
                    ctFor,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of("suggestion", "%s.addAll(Collections.nCopies(%s, %s))".formatted(
                            ctVariableRead,
                            forLoopRange.length(),
                            rhs
                        ))
                    ),
                    ProblemType.COLLECTIONS_N_COPIES
                );
            }
        });
    }
}
