package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSwitchExpression;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = { ProblemType.CLOSED_SET_OF_VALUES })
public class ClosedSetOfValues extends IntegratedCheck {
    private static final int MIN_SET_SIZE = 3;
    private static final int MAX_SET_SIZE = 12;
    private static final List<Class<?>> SUPPORTED_TYPES = List.of(
        java.lang.String.class,
        java.lang.Character.class,
        char.class
    );

    public ClosedSetOfValues() {
        super(new LocalizedMessage("closed-set-of-values"));
    }

    private static boolean isSupportedType(CtTypeReference<?> ctTypeReference) {
        return SUPPORTED_TYPES
                .stream()
                .map((Class<?> e) -> ctTypeReference.getFactory().Type().createReference(e))
                .anyMatch(ty -> ty.equals(ctTypeReference));
    }

    private static List<CtExpression<?>> parseOfExpression(CtExpression<?> ctExpression) {
        var supportedCollections = Stream.of(
            java.util.List.class,
            java.util.Set.class,
            java.util.Collection.class
        ).map((Class<?> e) -> ctExpression.getFactory().Type().createReference(e));

        List<CtExpression<?>> result = new ArrayList<>();

        CtTypeReference<?> expressionType = ctExpression.getType();
        if (supportedCollections.noneMatch(ty -> ty.equals(expressionType) || expressionType.isSubtypeOf(ty))) {
            return result;
        }

        if (ctExpression instanceof CtInvocation<?> ctInvocation
            && ctInvocation.getTarget() instanceof CtTypeAccess<?>) {
            CtExecutableReference<?> ctExecutableReference = ctInvocation.getExecutable();
            if (ctExecutableReference.getSimpleName().equals("of")) {
                result.addAll(ctInvocation.getArguments());
            }
        }

        return result;
    }

    private void checkSwitch(StaticAnalysis staticAnalysis, CtAbstractSwitch<?> ctSwitch) {
        CtTypeReference<?> ctTypeReference = ctSwitch.getSelector().getType();

        if (!isSupportedType(ctTypeReference)) {
            return;
        }

        int numberOfCases = ctSwitch.getCases().size();
        if (numberOfCases < MIN_SET_SIZE || numberOfCases > MAX_SET_SIZE) {
            return;
        }

        boolean areKnown = ctSwitch.getCases()
                .stream()
                .flatMap((CtCase<?> e) -> e.getCaseExpressions().stream())
                .map(e -> SpoonUtil.resolveCtExpression(staticAnalysis, e))
                .allMatch(e -> e instanceof CtLiteral<?>);

        if (areKnown) {
            addLocalProblem(
                ctSwitch,
                new LocalizedMessage("closed-set-of-values-switch"),
                ProblemType.CLOSED_SET_OF_VALUES
            );
        }
    }

    private void checkFiniteListing(StaticAnalysis staticAnalysis, CtExpression<?> ctExpression, List<CtExpression<?>> values) {
        // resolve list of constants
        List<CtExpression<?>> elements = values
            .stream()
            .map(e -> SpoonUtil.resolveCtExpression(staticAnalysis, e))
            .collect(Collectors.toList());

        // check the size on the distinct elements (to avoid linting e.g. List.of(0, 0, 0, 0, 0))
        Collection<CtExpression<?>> distinctElements = new HashSet<>(elements);
        if (distinctElements.size() < MIN_SET_SIZE || distinctElements.size() > MAX_SET_SIZE) {
            return;
        }

        boolean areKnown = elements.stream().allMatch(e -> e instanceof CtLiteral<?>);
        boolean areAllOfSupportedType = elements.stream().allMatch(e -> isSupportedType(e.getType()));

        if (areKnown && areAllOfSupportedType) {
            addLocalProblem(
                ctExpression,
                new LocalizedMessage("closed-set-of-values-list"),
                ProblemType.CLOSED_SET_OF_VALUES
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
                checkSwitch(staticAnalysis, switchStatement);
                super.visitCtSwitch(switchStatement);
            }

            @Override
            public <T, S> void visitCtSwitchExpression(CtSwitchExpression<T, S> switchExpression) {
                checkSwitch(staticAnalysis, switchExpression);
                super.visitCtSwitchExpression(switchExpression);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                if (!SpoonUtil.isEffectivelyFinal(staticAnalysis, ctField.getReference())) {
                    return;
                }

                CtExpression<?> ctExpression = ctField.getDefaultExpression();
                if (ctExpression == null) {
                    return;
                }

                if (ctExpression.isImplicit()) {
                    return;
                }

                if (ctField.getType().isArray() && ctExpression instanceof CtNewArray<?> ctNewArray) {
                    checkFiniteListing(staticAnalysis, ctExpression, ctNewArray.getElements());
                } else {
                    checkFiniteListing(staticAnalysis, ctExpression, parseOfExpression(ctExpression));
                }

                super.visitCtField(ctField);
            }
        });
    }
}
