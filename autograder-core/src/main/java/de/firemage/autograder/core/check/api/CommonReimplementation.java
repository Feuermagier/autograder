package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
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
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY,
    ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT,
    ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN,
    ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL,
    ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL,
    ProblemType.COMMON_REIMPLEMENTATION_MODULO,
    ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES,
    ProblemType.COMMON_REIMPLEMENTATION_SUBLIST
})
public class CommonReimplementation extends IntegratedCheck {
    private void checkStringRepeat(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }

        // lhs += rhs
        if (statements.get(0) instanceof CtOperatorAssignment<?, ?> ctAssignment
            && ctAssignment.getKind() == BinaryOperatorKind.PLUS) {
            CtExpression<?> lhs = ctAssignment.getAssigned();
            if (!SpoonUtil.isTypeEqualTo(lhs.getType(), String.class)) {
                return;
            }

            CtExpression<?> rhs = SpoonUtil.resolveCtExpression(ctAssignment.getAssignment());
            // return if the for loop uses the loop variable (would not be a simple repetition)
            if (!ctAssignment.getElements(new VariableAccessFilter<>(forLoopRange.loopVariable())).isEmpty()) {
                return;
            }

            // return if the rhs uses the lhs: lhs += rhs + lhs
            if (lhs instanceof CtVariableAccess<?> ctVariableAccess && !rhs.getElements(new VariableAccessFilter<>(ctVariableAccess.getVariable())).isEmpty()) {
                return;
            }

            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // string.repeat(count)
                        "suggestion", "%s += %s".formatted(
                            lhs.prettyprint(),
                            rhs.getFactory().createInvocation(
                                rhs,
                                rhs.getFactory().Type().get(java.lang.String.class)
                                    .getMethod("repeat", rhs.getFactory().createCtTypeReference(int.class))
                                    .getReference(),
                                forLoopRange.length()
                            ).prettyprint())
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT
            );
        }
    }

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

    private void checkMaxMin(CtIf ctIf) {
        Set<BinaryOperatorKind> maxOperators = Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.LE);
        Set<BinaryOperatorKind> minOperators = Set.of(BinaryOperatorKind.GT, BinaryOperatorKind.GE);

        // happens for if (a);
        if (ctIf.getThenStatement() == null) {
            return;
        }

        // ensure that in the if block there is only one assignment to a variable
        // and the condition is a binary operator with <, <=, > or >=
        List<CtStatement> thenBlock = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
        if (thenBlock.size() != 1
            || !(thenBlock.get(0) instanceof CtAssignment<?, ?> thenAssignment)
            || !(thenAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
            || !(ctIf.getCondition() instanceof CtBinaryOperator<Boolean> ctBinaryOperator)
            || (!maxOperators.contains(ctBinaryOperator.getKind()) && !minOperators.contains(ctBinaryOperator.getKind()))) {
            return;
        }

        // keep track of the assigned variable (must be the same in the else block)
        CtVariableReference<?> assignedVariable = ctVariableWrite.getVariable();

        // this is the value that is assigned if the then-block is not executed
        // The variable is not changed without an else-Block (this would be equivalent to an else { variable = variable; })
        CtExpression<?> elseValue = ctIf.getFactory().createVariableRead(
            assignedVariable,
            assignedVariable.getModifiers().contains(ModifierKind.STATIC)
        );
        if (ctIf.getElseStatement() != null) {
            List<CtStatement> elseBlock = SpoonUtil.getEffectiveStatements(ctIf.getElseStatement());
            if (elseBlock.size() != 1
                || !(elseBlock.get(0) instanceof CtAssignment<?,?> elseAssignment)
                || !(elseAssignment.getAssigned() instanceof CtVariableAccess<?> elseAccess)
                // ensure that the else block assigns to the same variable
                || !elseAccess.getVariable().equals(assignedVariable)) {
                return;
            }

            elseValue = elseAssignment.getAssignment();
        }

        CtBinaryOperator<Boolean> condition = ctBinaryOperator;
        // ensure that the else value is on the left side of the condition
        if (ctBinaryOperator.getRightHandOperand().equals(elseValue)) {
            condition = SpoonUtil.swapCtBinaryOperator(condition);
        }

        // if it is not on either side of the condition, return
        if (!condition.getLeftHandOperand().equals(elseValue)) {
            return;
        }

        // max looks like this:
        // if (variable < max) {
        //     variable = max;
        // }
        //
        // or with an explicit else block:
        //
        // if (max > expr) {
        //     v = max;
        // } else {
        //     v = expr;
        // }

        if (maxOperators.contains(condition.getKind())) {
            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s = Math.max(%s, %s)".formatted(
                            ctVariableWrite.prettyprint(),
                            elseValue.prettyprint(),
                            condition.getRightHandOperand().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
            );

            return;
        }

        // if (variable > min) {
        //    variable = min;
        // }

        if (minOperators.contains(condition.getKind())) {
            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s = Math.min(%s, %s)".formatted(
                            ctVariableWrite.prettyprint(),
                            elseValue.prettyprint(),
                            condition.getRightHandOperand().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
            );

            return;
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

        CtExpression<?> rhs = ctAssignment.getAssignment();
        if (!SpoonUtil.isImmutable(rhs.getType())) {
            return;
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "Arrays.fill(%s, %s, %s, %s)".formatted(
                        ctArrayWrite.getTarget().prettyprint(),
                        forLoopRange.start().prettyprint(),
                        forLoopRange.end().prettyprint(),
                        ctAssignment.getAssignment().prettyprint()
                    )
                )
            ),
            ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL
        );
    }

    private void checkModulo(CtIf ctIf) {
        List<CtStatement> thenBlock = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
        if (ctIf.getElseStatement() != null
            || thenBlock.size() != 1
            || !(thenBlock.get(0) instanceof CtAssignment<?, ?> thenAssignment)
            || !(thenAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
            || !(ctIf.getCondition() instanceof CtBinaryOperator<Boolean> ctBinaryOperator)
            || !Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.LE, BinaryOperatorKind.GE, BinaryOperatorKind.GT, BinaryOperatorKind.EQ)
                .contains(ctBinaryOperator.getKind())) {
            return;
        }

        // must assign a value of 0
        if (!(SpoonUtil.resolveCtExpression(thenAssignment.getAssignment()) instanceof CtLiteral<?> ctLiteral)
            || !(ctLiteral.getValue() instanceof Integer integer)
            || integer != 0) {
            return;
        }

        CtVariableReference<?> assignedVariable = ctVariableWrite.getVariable();

        CtBinaryOperator<Boolean> condition = SpoonUtil.normalizeBy(
            (left, right) -> right instanceof CtVariableAccess<?> ctVariableAccess && ctVariableAccess.getVariable().equals(assignedVariable),
            ctBinaryOperator
        );

        // the assigned variable is not on either side
        if (!(condition.getLeftHandOperand() instanceof CtVariableAccess<?> ctVariableAccess)
            || !(ctVariableAccess.getVariable().equals(assignedVariable))) {
            return;
        }

        // if (variable >= 3) {
        //    variable = 0;
        // }
        //
        // is equal to
        //
        // variable %= 3;
        if (Set.of(BinaryOperatorKind.GE, BinaryOperatorKind.EQ).contains(condition.getKind())) {
            CtExpression<?> right = condition.getRightHandOperand();

            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s %%= %s".formatted(
                            assignedVariable.prettyprint(),
                            right.prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MODULO
            );
        }
    }

    private record AddInvocation(
        CtVariableReference<?> collection,
        CtExecutableReference<?> executableReference,
        CtEnumFieldRead ctEnumFieldRead
    ) {
        public static Optional<AddInvocation> of(CtStatement ctStatement) {
            CtType<?> collectionType = ctStatement.getFactory().Type().get(java.util.Collection.class);
            if (!(ctStatement instanceof CtInvocation<?> ctInvocation)
                || !(ctInvocation.getTarget() instanceof CtVariableAccess<?> ctVariableAccess)
                || ctVariableAccess.getVariable().getType() instanceof CtTypeParameterReference
                || !ctVariableAccess.getVariable().getType().isSubtypeOf(collectionType.getReference())) {
                return Optional.empty();
            }

            CtExecutableReference<?> executableReference = ctInvocation.getExecutable();
            CtVariableReference<?> collection = ctVariableAccess.getVariable();
            if (!SpoonUtil.isSignatureEqualTo(
                executableReference,
                boolean.class,
                "add",
                Object.class)) {
                return Optional.empty();
            }

            return CtEnumFieldRead.of(ctInvocation.getArguments().get(0))
                .map(fieldRead -> new AddInvocation(collection, executableReference, fieldRead));
        }
    }

    private static <T> boolean isOrderedCollection(CtTypeReference<T> ctTypeReference) {
        return Stream.of(java.util.List.class)
            .map(ctClass -> ctTypeReference.getFactory().createCtTypeReference(ctClass))
            .anyMatch(ctTypeReference::isSubtypeOf);
    }

    private void checkEnumValues(
        CtEnum<?> ctEnum,
        boolean isOrdered,
        List<? extends CtEnumValue<?>> enumValues,
        UnaryOperator<? super String> suggestion,
        CtElement span
    ) {
        List<CtEnumValue<?>> expectedValues = new ArrayList<>(ctEnum.getEnumValues());

        for (CtEnumValue<?> enumValue : enumValues) {
            // check for out of order add
            if (isOrdered && !expectedValues.isEmpty() && !expectedValues.get(0).equals(enumValue)) {
                return;
            }

            boolean wasPresent = expectedValues.remove(enumValue);

            // check for duplicate or out of order add
            if (!wasPresent) {
                return;
            }
        }

        if (expectedValues.isEmpty() && !enumValues.isEmpty()) {
            this.addLocalProblem(
                span == null ? enumValues.get(enumValues.size() - 1) : span,
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

        if (ctEnum != null) {
            this.checkEnumValues(
                ctEnum,
                isOrdered,
                addedValues,
                suggestion,
                span
            );
        }
    }

    private void checkAddAllEnumValues(CtBlock<?> ctBlock) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctBlock);

        CtVariableReference<?> collection = null;
        CtEnum<?> ctEnum = null;
        List<CtEnumValue<?>> addedValues = new ArrayList<>();

        for (CtStatement ctStatement : statements) {
            AddInvocation addInvocation = AddInvocation.of(ctStatement).orElse(null);
            if (addInvocation == null) {
                collection = null;
                ctEnum = null;
                continue;
            }

            // ensure that all invocations refer to the same collection
            if (collection == null) {
                collection = addInvocation.collection();
            }

            if (!collection.equals(addInvocation.collection())) {
                // if there are multiple collections, just invalidate the data
                collection = null;
                ctEnum = null;
                continue;
            }

            if (ctEnum == null) {
                ctEnum = addInvocation.ctEnumFieldRead().ctEnum();
                addedValues = new ArrayList<>();
            }

            addedValues.add(addInvocation.ctEnumFieldRead().ctEnumValue());

            if (addedValues.size() == ctEnum.getEnumValues().size()) {
                String collectionName = collection.getSimpleName();
                this.checkEnumValues(
                    ctEnum,
                    isOrderedCollection(collection.getType()),
                    addedValues,
                    suggestion -> "%s.addAll(Arrays.asList(%s))".formatted(collectionName, suggestion),
                    null
                );

                collection = null;
                ctEnum = null;
            }
        }
    }

    private void checkSubList(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        if (forLoopRange == null) {
            return;
        }

        // ensure that the variable is only used to access the list elements via get
        CtVariable<?> ctListVariable = null;
        CtTypeReference<?> listType = ctFor.getFactory().Type().get(java.util.List.class).getReference();
        for (CtElement use : SpoonUtil.findUsesIn(forLoopRange.loopVariable().getDeclaration(), ctFor.getBody())) {
            if (!(use instanceof CtVariableAccess<?> variableAccess)
                || !(variableAccess.getParent() instanceof CtInvocation<?> ctInvocation)
                || !(SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), Object.class, "get", int.class))
                || !(ctInvocation.getTarget() instanceof CtVariableAccess<?> ctVariableAccess)
                || !ctVariableAccess.getType().isSubtypeOf(listType)
                || (ctListVariable != null && !ctListVariable.getReference().equals(ctVariableAccess.getVariable()))) {
                return;
            }

            if (ctListVariable == null) {
                ctListVariable = ctVariableAccess.getVariable().getDeclaration();
            }
        }

        if (ctListVariable == null) {
            return;
        }

        CtTypeReference<?> listElementType = ctFor.getFactory().createCtTypeReference(java.lang.Object.class);
        // size != 1, if the list is a raw type: List list = new ArrayList();
        if (ctListVariable.getType().getActualTypeArguments().size() == 1) {
            listElementType = ctListVariable.getType().getActualTypeArguments().get(0);
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "for (%s value : %s.subList(%s, %s)) { ... }".formatted(
                        listElementType.prettyprint(),
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
                checkStringRepeat(ctFor);
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
            public void visitCtIf(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition()) {
                    super.visitCtIf(ctIf);
                    return;
                }

                checkMaxMin(ctIf);
                checkModulo(ctIf);
                super.visitCtIf(ctIf);
            }

            @Override
            public <R> void visitCtBlock(CtBlock<R> ctBlock) {
                if (ctBlock.isImplicit() || !ctBlock.getPosition().isValidPosition()) {
                    super.visitCtBlock(ctBlock);
                    return;
                }

                checkAddAllEnumValues(ctBlock);

                super.visitCtBlock(ctBlock);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                if (!SpoonUtil.isEffectivelyFinal(ctField.getReference())) {
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
