package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.UNNECESSARY_BOXING })
public class UnnecessaryBoxing extends IntegratedCheck {
    private static final Set<Class<?>> BOXED_TYPES = Set.of(
        Boolean.class,
        Byte.class,
        Character.class,
        Float.class,
        Integer.class,
        Long.class,
        Short.class,
        Double.class
    );

    private static boolean isBoxedType(CtTypeReference<?> ctTypeReference) {
        return BOXED_TYPES.stream().anyMatch(ty -> SpoonUtil.isTypeEqualTo(ctTypeReference, ty));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypeReference<?>>() {
            @Override
            public void process(CtTypeReference<?> ctTypeReference) {
                if (ctTypeReference.isImplicit()
                    || !ctTypeReference.getPosition().isValidPosition()
                    || !isBoxedType(ctTypeReference)) {
                    return;
                }

                CtVariable<?> ctVariable = ctTypeReference.getParent(CtVariable.class);
                if (ctVariable != null && isBoxedType(ctVariable.getType())) {
                    List<CtExpression<?>> assignedValues = staticAnalysis.getModel()
                        .getElements(new VariableAccessFilter<>(ctVariable.getReference()))
                        .stream()
                        .filter(ctVariableAccess -> ctVariableAccess instanceof CtVariableWrite<?>
                            && ctVariableAccess.getParent() instanceof CtAssignment<?,?>)
                        .map(ctVariableAccess -> ((CtAssignment<?, ?>) ctVariableAccess.getParent()).getAssignment())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new));

                    if (ctVariable.getDefaultExpression() != null) {
                        assignedValues.add(ctVariable.getDefaultExpression());
                    }

                    boolean canBeNull = assignedValues.stream()
                        .anyMatch(value -> SpoonUtil.isNullLiteral(value) || isBoxedType(value.getType()))
                        || assignedValues.isEmpty()
                        || ctVariable.getDefaultExpression() == null;

                    if (canBeNull) {
                        return;
                    }

                    addLocalProblem(
                        ctVariable,
                        new LocalizedMessage(
                            "unnecessary-boxing",
                            Map.of(
                                "suggestion", ctVariable.getType().unbox().getSimpleName()
                            )
                        ),
                        ProblemType.UNNECESSARY_BOXING
                    );
                }
            }
        });
    }
}
