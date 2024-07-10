package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.effects.AssignmentEffect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSwitchExpression;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.CLOSED_SET_OF_VALUES })
public class ClosedSetOfValues extends IntegratedCheck {
    private static final int MIN_SET_SIZE = 3;
    private static final int MAX_SET_SIZE = 12;
    private static final List<Class<?>> SUPPORTED_TYPES = List.of(
        java.lang.String.class,
        java.lang.Character.class,
        char.class
    );

    private static boolean isSupportedType(CtTypeReference<?> ctTypeReference) {
        return TypeUtil.isTypeEqualTo(ctTypeReference, SUPPORTED_TYPES.toArray(new Class[0]));
    }

    private boolean shouldSwitchOverEnum(CtAbstractSwitch<?> ctSwitch) {
        for (CtCase<?> ctCase : ctSwitch.getCases()) {
            // skip default case
            if (ctCase.getCaseExpressions().isEmpty()) {
                continue;
            }

            var effect = SpoonUtil.getSingleEffect(ctCase.getStatements()).orElse(null);

            if (!(effect instanceof TerminalEffect || effect instanceof AssignmentEffect)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static Set<CtLiteral<?>> getDistinctElementsFromSwitch(CtAbstractSwitch<?> ctSwitch) {
        List<? extends CtExpression<?>> elements = ctSwitch.getCases()
            .stream()
            .flatMap((CtCase<?> e) -> e.getCaseExpressions().stream())
            .map(SpoonUtil::resolveCtExpression)
            .toList();

        if (elements.stream().anyMatch(e -> !(e instanceof CtLiteral<?>))) {
            return Set.of();
        }

        return distinctElements((List<CtLiteral<?>>) elements);
    }

    private void checkSwitch(CtAbstractSwitch<?> ctSwitch) {
        CtTypeReference<?> ctTypeReference = ctSwitch.getSelector().getType();

        if (!isSupportedType(ctTypeReference)) {
            return;
        }

        Set<CtLiteral<?>> distinctElements = getDistinctElementsFromSwitch(ctSwitch);
        if (distinctElements.size() < MIN_SET_SIZE || distinctElements.size() > MAX_SET_SIZE) {
            return;
        }

        if (this.shouldSwitchOverEnum(ctSwitch)) {
            addLocalProblem(
                ctSwitch,
                new LocalizedMessage(
                    "closed-set-of-values-switch",
                    Map.of(
                        "values",
                        distinctElements.stream()
                            .map(CtLiteral::toString)
                            .collect(Collectors.joining(", "))
                    )
                ),
                ProblemType.CLOSED_SET_OF_VALUES
            );
        }
    }

    private static Set<CtLiteral<?>> distinctElements(Collection<? extends CtLiteral<?>> elements) {
        return new LinkedHashSet<>(elements.stream()
            .filter(e -> e.getValue() != null)
            .toList());
    }

    private static boolean isFiniteSet(Collection<? extends CtLiteral<?>> distinctElements) {
        // check the size on the distinct elements (to avoid linting e.g. List.of(0, 0, 0, 0, 0))
        return distinctElements.size() >= MIN_SET_SIZE && distinctElements.size() <= MAX_SET_SIZE;
    }

    private static List<CtLiteral<?>> getFiniteSet(Iterable<? extends CtExpression<?>> elements) {
        List<CtLiteral<?>> result = new ArrayList<>();

        for (CtExpression<?> ctExpression : elements) {
            CtExpression<?> resolved = SpoonUtil.resolveCtExpression(ctExpression);

            if (!isSupportedType(resolved.getType()) || !(resolved instanceof CtLiteral<?> ctLiteral)) {
                return List.of();
            }

            result.add(ctLiteral);
        }

        return result;
    }

    private void checkFiniteListing(CtExpression<?> ctExpression, Iterable<? extends CtExpression<?>> values) {
        List<CtLiteral<?>> literals = getFiniteSet(values);
        Collection<CtLiteral<?>> distinctElements = distinctElements(literals);

        if (literals.isEmpty() || !isFiniteSet(distinctElements)) {
            return;
        }

        this.addLocalProblem(
            ctExpression,
            new LocalizedMessage(
                "closed-set-of-values-list",
                Map.of(
                    "values",
                    distinctElements.stream()
                        .map(CtLiteral::toString)
                        .collect(Collectors.joining(", "))
                )
            ),
            ProblemType.CLOSED_SET_OF_VALUES
        );
    }

    private void checkCtMethod(CtMethod<?> ctMethod) {
        CtTypeReference<?> returnType = ctMethod.getType();
        if (returnType == null || !isSupportedType(returnType)) {
            return;
        }

        List<CtReturn<?>> ctReturns = ctMethod.getElements(new TypeFilter<>(CtReturn.class));

        List<CtLiteral<?>> literals = getFiniteSet(ctReturns
            .stream()
            .map(CtReturn::getReturnedExpression)
            .toList());
        Collection<CtLiteral<?>> distinctElements = distinctElements(literals);

        if (literals.isEmpty() || !isFiniteSet(distinctElements)) {
            return;
        }

        this.addLocalProblem(
            ctMethod,
            new LocalizedMessage(
                "closed-set-of-values-method",
                Map.of(
                    "values",
                    distinctElements.stream()
                        .map(CtLiteral::toString)
                        .collect(Collectors.joining(", "))
                )
            ),
            ProblemType.CLOSED_SET_OF_VALUES
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
                checkSwitch(switchStatement);
                super.visitCtSwitch(switchStatement);
            }

            @Override
            public <T, S> void visitCtSwitchExpression(CtSwitchExpression<T, S> switchExpression) {
                checkSwitch(switchExpression);
                super.visitCtSwitchExpression(switchExpression);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                CtExpression<?> ctExpression = ctField.getDefaultExpression();
                if (ctExpression == null) {
                    return;
                }

                if (ctExpression.isImplicit()) {
                    return;
                }

                if (!SpoonUtil.isEffectivelyFinal(ctField)) {
                    return;
                }

                if (ctField.getType().isArray() && ctExpression instanceof CtNewArray<?> ctNewArray) {
                    checkFiniteListing(ctExpression, ctNewArray.getElements());
                } else {
                    checkFiniteListing(ctExpression, SpoonUtil.getElementsOfExpression(ctExpression));
                }

                super.visitCtField(ctField);
            }

            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> ctLocalVariable) {
                CtExpression<?> ctExpression = ctLocalVariable.getDefaultExpression();
                if (ctExpression == null) {
                    return;
                }

                if (ctExpression.isImplicit()) {
                    return;
                }

                if (!SpoonUtil.isEffectivelyFinal(ctLocalVariable)) {
                    return;
                }

                if (ctLocalVariable.getType().isArray() && ctExpression instanceof CtNewArray<?> ctNewArray) {
                    checkFiniteListing(ctExpression, ctNewArray.getElements());
                } else {
                    checkFiniteListing(ctExpression, SpoonUtil.getElementsOfExpression(ctExpression));
                }

                super.visitCtLocalVariable(ctLocalVariable);
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                checkCtMethod(ctMethod);
                super.visitCtMethod(ctMethod);
            }
        });
    }
}
