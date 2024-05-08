package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.uses.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.UNNECESSARY_BOXING })
public class UnnecessaryBoxing extends IntegratedCheck {
    private static boolean isBoxedType(CtTypeReference<?> ctTypeReference) {
        // a type is boxed if it changes when unboxed
        return !ctTypeReference.equals(ctTypeReference.unbox());
    }

    private static <T> boolean isLikelyNull(CtExpression<T> ctExpression) {
        return ctExpression == null
            || SpoonUtil.isNullLiteral(ctExpression)
            || isBoxedType(ctExpression.getType());
    }

    private <T> void checkVariable(CtVariable<T> ctVariable, CodeModel model) {
        if (ctVariable.isImplicit() || !ctVariable.getPosition().isValidPosition()) {
            return;
        }

        CtTypeReference<?> ctTypeReference = ctVariable.getType();
        if (ctTypeReference == null || !isBoxedType(ctTypeReference)) {
            return;
        }

        boolean hasNullAssigned = isLikelyNull(ctVariable.getDefaultExpression()) || UsesFinder.variableUses(ctVariable)
                .ofType(CtVariableWrite.class)
                .filter(use ->
                        use.getParent() instanceof CtAssignment<?,?> assignment
                                && assignment.getAssignment() != null
                                && isLikelyNull(assignment.getAssignment())
                ).hasAny();

        if (!hasNullAssigned) {
            addLocalProblem(
                ctVariable,
                new LocalizedMessage(
                    "suggest-replacement",
                    Map.of(
                        "original", ctTypeReference.getSimpleName(),
                        "suggestion", ctTypeReference.unbox().getSimpleName()
                    )
                ),
                ProblemType.UNNECESSARY_BOXING
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                checkVariable(ctVariable, staticAnalysis.getCodeModel());
            }
        });
    }
}
