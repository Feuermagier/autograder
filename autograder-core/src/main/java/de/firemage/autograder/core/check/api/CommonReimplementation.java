package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.general.ForToForEachLoop;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY,
    ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL,
    ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL,
    ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES,
    ProblemType.COMMON_REIMPLEMENTATION_SUBLIST
})
public class CommonReimplementation extends IntegratedCheck {
    private void checkArrayCopy(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }


        if (statements.get(0) instanceof CtAssignment<?, ?> ctAssignment
            && !(ctAssignment instanceof CtOperatorAssignment<?, ?>)
            && ctAssignment.getAssigned() instanceof CtArrayAccess<?, ?> lhs
            && ctAssignment.getAssignment() instanceof CtArrayAccess<?, ?> rhs
            && lhs.getTarget() != null
            && rhs.getTarget() != null
            && lhs.getIndexExpression().equals(rhs.getIndexExpression())
            && lhs.getIndexExpression() instanceof CtVariableRead<Integer> index
            && index.getVariable().equals(forLoopRange.loopVariable())) {
            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // System.arraycopy(src, srcPos, dest, destPos, length)
                        "suggestion", "System.arraycopy(%s, %s, %s, %s, %s)".formatted(
                            rhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            lhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            forLoopRange.length().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY
            );
        }
    }

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

    private void checkArraysFill(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());

        if (statements.size() != 1
            || forLoopRange == null
            || !(statements.get(0) instanceof CtAssignment<?, ?> ctAssignment)
            || !(ctAssignment.getAssigned() instanceof CtArrayWrite<?> ctArrayWrite)
            || !(ctArrayWrite.getIndexExpression() instanceof CtVariableRead<?> index)
            || !(index.getVariable().equals(forLoopRange.loopVariable()))) {
            return;
        }

        // return if the for loop uses the loop variable (would not be a simple repetition)
        if (!ctAssignment.getAssignment().getElements(new VariableAccessFilter<>(forLoopRange.loopVariable())).isEmpty()) {
            return;
        }

        // ignore new array or new class assignments
        if (ctAssignment.getAssignment() instanceof CtNewClass<?> || ctAssignment.getAssignment() instanceof CtNewArray<?>) {
            return;
        }

        CtExpression<?> rhs = ctAssignment.getAssignment();
        if (!SpoonUtil.isImmutable(rhs.getType())) {
            return;
        }

        String suggestion = "Arrays.fill(%s, %s, %s, %s)".formatted(
            ctArrayWrite.getTarget().prettyprint(),
            forLoopRange.start().prettyprint(),
            forLoopRange.end().prettyprint(),
            ctAssignment.getAssignment().prettyprint()
        );
        if (forLoopRange.start() instanceof CtLiteral<Integer> ctLiteral
            && ctLiteral.getValue() == 0
            && forLoopRange.end() instanceof CtFieldAccess<Integer> fieldAccess
            && ctArrayWrite.getTarget().equals(fieldAccess.getTarget())
            && fieldAccess.getVariable().getSimpleName().equals("length")) {
            suggestion = "Arrays.fill(%s, %s)".formatted(
                ctArrayWrite.getTarget().prettyprint(),
                ctAssignment.getAssignment().prettyprint()
            );
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of("suggestion", suggestion)
            ),
            ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL
        );
    }

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

    private void checkSubList(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        if (forLoopRange == null) {
            return;
        }

        // ensure that the variable is only used to access the list elements via get
        CtVariable<?> ctListVariable = ForToForEachLoop.getForEachLoopVariable(
            ctFor,
            forLoopRange,
            ForToForEachLoop.LOOP_VARIABLE_ACCESS_LIST
        ).orElse(null);

        if (ctListVariable == null) {
            return;
        }

        CtTypeReference<?> listElementType = ctFor.getFactory().createCtTypeReference(java.lang.Object.class);
        // size != 1, if the list is a raw type: List list = new ArrayList();
        if (ctListVariable.getType().getActualTypeArguments().size() == 1) {
            listElementType = ctListVariable.getType().getActualTypeArguments().get(0);
        }

        // check if the loop iterates over the whole list (then it is covered by the foreach loop check)
        if (SpoonUtil.resolveConstant(forLoopRange.start()) instanceof CtLiteral<Integer> ctLiteral
            && ctLiteral.getValue() == 0
            && ForToForEachLoop.findIterable(forLoopRange).isPresent()) {
            return;
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "for (%s value : %s.subList(%s, %s)) { ... }".formatted(
                        listElementType.unbox().prettyprint(),
                        ctListVariable.getSimpleName(),
                        forLoopRange.start().prettyprint(),
                        forLoopRange.end().prettyprint()
                    )
                )
            ),
            ProblemType.COMMON_REIMPLEMENTATION_SUBLIST
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtFor(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    super.visitCtFor(ctFor);
                    return;
                }

                checkArrayCopy(ctFor);
                checkArraysFill(ctFor);
                checkSubList(ctFor);
                super.visitCtFor(ctFor);
            }

            @Override
            public void visitCtForEach(CtForEach ctForEach) {
                if (ctForEach.isImplicit() || !ctForEach.getPosition().isValidPosition()) {
                    super.visitCtForEach(ctForEach);
                    return;
                }

                checkAddAll(ctForEach);
                super.visitCtForEach(ctForEach);
            }

            @Override
            public <R> void visitCtBlock(CtBlock<R> ctBlock) {
                if (ctBlock.isImplicit() || !ctBlock.getPosition().isValidPosition()) {
                    super.visitCtBlock(ctBlock);
                    return;
                }

                super.visitCtBlock(ctBlock);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                if (!SpoonUtil.isEffectivelyFinal(ctField)) {
                    super.visitCtField(ctField);
                    return;
                }

                CtExpression<?> ctExpression = ctField.getDefaultExpression();
                if (ctExpression == null || ctExpression.isImplicit() || !ctExpression.getPosition().isValidPosition()) {
                    super.visitCtField(ctField);
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
                        SpoonUtil.getElementsOfExpression(ctExpression),
                        suggestion -> "%s.of(%s)".formatted(ctExpression.getType().getSimpleName(), suggestion),
                        ctExpression
                    );
                }

                super.visitCtField(ctField);
            }
        });
    }
}
