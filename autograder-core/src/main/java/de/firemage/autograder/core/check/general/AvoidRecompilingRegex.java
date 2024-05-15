package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.filter.DirectReferenceFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.AVOID_RECOMPILING_REGEX })
public class AvoidRecompilingRegex extends IntegratedCheck {
    private boolean isPatternInvocation(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), java.util.regex.Pattern.class)
            && List.of("matches", "compile").contains(ctInvocation.getExecutable().getSimpleName());
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<String>>() {
            @Override
            public void process(CtField<String> ctField) {
                if (ctField.isImplicit()
                    || !ctField.getPosition().isValidPosition()
                    || !SpoonUtil.isTypeEqualTo(ctField.getType(), String.class)
                    || ctField.getDefaultExpression() == null) {
                    return;
                }

                CtExpression<?> ctExpression = SpoonUtil.resolveCtExpression(ctField.getDefaultExpression());

                // skip all non-literals to improve performance
                if (!(ctExpression instanceof CtLiteral<?>)) {
                    return;
                }

                List<CtReference> references = staticAnalysis.getModel()
                    .getElements(new DirectReferenceFilter<>(ctField.getReference()));

                boolean isPattern = false;

                for (CtReference ctReference : references) {
                    CtInvocation<?> ctInvocation = ctReference.getParent(CtInvocation.class);
                    if (ctInvocation == null || !isPatternInvocation(ctInvocation)) {
                        return;
                    }

                    isPattern = true;
                }

                if (isPattern) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage(
                            "avoid-recompiling-regex",
                            Map.of(
                                "suggestion", "Pattern.compile(" + ctExpression.prettyprint() + ")"
                            )
                        ),
                        ProblemType.AVOID_RECOMPILING_REGEX
                    );
                }
            }
        });
    }
}
