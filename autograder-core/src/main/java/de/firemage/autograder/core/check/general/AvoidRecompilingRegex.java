package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.AVOID_RECOMPILING_REGEX })
public class AvoidRecompilingRegex extends IntegratedCheck {
    private boolean isPatternInvocation(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && TypeUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), java.util.regex.Pattern.class)
            && List.of("matches", "compile").contains(ctInvocation.getExecutable().getSimpleName());
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<String>>() {
            @Override
            public void process(CtField<String> ctField) {
                if (ctField.isImplicit()
                    || !ctField.getPosition().isValidPosition()
                    || !TypeUtil.isTypeEqualTo(ctField.getType(), String.class)
                    || ctField.getDefaultExpression() == null) {
                    return;
                }

                CtExpression<?> ctExpression = SpoonUtil.resolveCtExpression(ctField.getDefaultExpression());

                // skip all non-literals to improve performance
                if (!(ctExpression instanceof CtLiteral<?>)) {
                    return;
                }

                boolean isPattern = UsesFinder.variableUses(ctField)
                    .hasAnyAndAllMatch(ctVariableAccess -> ctVariableAccess.getParent() instanceof CtInvocation<?> ctInvocation
                        && isPatternInvocation(ctInvocation));

                if (isPattern) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage(
                            "avoid-recompiling-regex",
                            Map.of(
                                "suggestion", "Pattern.compile(" + ctExpression + ")"
                            )
                        ),
                        ProblemType.AVOID_RECOMPILING_REGEX
                    );
                }
            }
        });
    }
}
