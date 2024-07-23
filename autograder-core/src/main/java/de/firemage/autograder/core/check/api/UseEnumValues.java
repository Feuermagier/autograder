package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.VariableUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES})
public class UseEnumValues extends IntegratedCheck {

    private static <T> boolean isOrderedCollection(CtTypeReference<T> ctTypeReference) {
        return Stream.of(java.util.List.class)
            .map(ctClass -> ctTypeReference.getFactory().createCtTypeReference(ctClass))
            .anyMatch(ctTypeReference::isSubtypeOf);
    }

    public static boolean checkEnumValues(
        CtEnum<?> ctEnum,
        boolean isOrdered,
        Collection<? extends CtEnumValue<?>> enumValues
    ) {
        List<CtEnumValue<?>> expectedValues = new ArrayList<>(ctEnum.getEnumValues());

        for (CtEnumValue<?> enumValue : enumValues) {
            // check for out of order add
            if (isOrdered && !expectedValues.isEmpty() && !expectedValues.get(0).equals(enumValue)) {
                return false;
            }

            boolean wasPresent = expectedValues.remove(enumValue);

            // check for duplicate or out of order add
            if (!wasPresent) {
                return false;
            }
        }

        return expectedValues.isEmpty() && !enumValues.isEmpty();
    }

    public record CtEnumFieldRead(CtEnum<?> ctEnum, CtEnumValue<?> ctEnumValue) {
        public static Optional<CtEnumFieldRead> of(CtExpression<?> ctExpression) {
            // this is a workaround for https://github.com/INRIA/spoon/issues/5412
            if (ctExpression.getType().equals(ctExpression.getFactory().Type().nullType())) {
                return Optional.empty();
            }

            // check if the expression is an enum type
            if (!ctExpression.getType().isEnum()
                // that it accesses a variant of the enum
                || !(ctExpression instanceof CtFieldRead<?> ctFieldRead)
                // that the field is a variant of the enum
                || !(ctFieldRead.getVariable().getDeclaration() instanceof CtEnumValue<?> ctEnumValue)
                // that the field is in an enum
                || !(ctEnumValue.getDeclaringType() instanceof CtEnum<?> ctEnum)) {
                return Optional.empty();
            }

            return Optional.of(new CtEnumFieldRead(ctEnum, ctEnumValue));
        }
    }

    private void checkListingEnumValues(
        boolean isOrdered,
        Iterable<? extends CtExpression<?>> ctExpressions,
        UnaryOperator<? super String> suggestion,
        CtElement span
    ) {
        CtEnum<?> ctEnum = null;
        List<CtEnumValue<?>> addedValues = new ArrayList<>();

        for (CtExpression<?> ctExpression : ctExpressions) {
            CtEnumFieldRead enumFieldRead = CtEnumFieldRead.of(ctExpression).orElse(null);
            if (enumFieldRead == null) {
                return;
            }

            if (ctEnum == null) {
                ctEnum = enumFieldRead.ctEnum();
            } else if (!ctEnum.equals(enumFieldRead.ctEnum())) {
                return;
            }

            addedValues.add(enumFieldRead.ctEnumValue());
        }

        if (ctEnum != null && checkEnumValues(ctEnum, isOrdered, addedValues)) {
            this.addLocalProblem(
                span == null ? addedValues.get(addedValues.size() - 1) : span,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", suggestion.apply("%s.values()".formatted(ctEnum.getSimpleName()))
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES
            );
        }
    }



    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> ctField) {
                if (!VariableUtil.isEffectivelyFinal(ctField)) {
                    return;
                }

                CtExpression<?> ctExpression = ctField.getDefaultExpression();
                if (ctExpression == null || ctExpression.isImplicit() || !ctExpression.getPosition().isValidPosition()) {
                    return;
                }

                if (ctField.getType().isArray() && ctExpression instanceof CtNewArray<?> ctNewArray) {
                    checkListingEnumValues(
                        true,
                        ctNewArray.getElements(),
                        suggestion -> "Arrays.copyOf(%s, %s.length)".formatted(suggestion, suggestion),
                        ctExpression
                    );
                } else {
                    checkListingEnumValues(
                        isOrderedCollection(ctExpression.getType()),
                        ExpressionUtil.getElementsOfExpression(ctExpression),
                        suggestion -> "%s.of(%s)".formatted(ctExpression.getType().getSimpleName(), suggestion),
                        ctExpression
                    );
                }

            }
        });
    }
}
