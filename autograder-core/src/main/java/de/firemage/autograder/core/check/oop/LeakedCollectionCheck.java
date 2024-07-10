package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.utils.Option;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.ElementUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// What should be supported:
// - Any mutable collection (List, Set, Map, ...)
// - Detect when things are always immutable (e.g. List.of() in constructor and no setter!)
// - Arrays that are leaked
// - Records that are not implemented correctly
// - Enums are a special case where the constructor is private?

// How can collections be modified from the outside?
// - The variable is passed through the constructor and the constructor does not copy it
// - The setter does not copy the list
// - The getter returns the list directly


// How to improve performance:
//
// 1. We visit each CtType and visit each CtConstructor
// 2. Based on the constructor we can infer if the field is
//    - not copied
//    - copied, but mutable
//    - immutable
// 3. Then we check each CtMethod if the field is assigned a parameter (-> mutable from outside) or returned a direct reference (-> leaked)
//
// A major problem is that one can reassign references, so this has to be taken into account.

@ExecutableCheck(reportedProblems = {
    ProblemType.LEAKED_COLLECTION_RETURN,
    ProblemType.LEAKED_COLLECTION_ASSIGN
})
public class LeakedCollectionCheck extends IntegratedCheck {
    private static boolean isMutableType(CtTypedElement<?> ctTypedElement) {
        return  ctTypedElement.getType().isArray() || TypeUtil.isSubtypeOf(ctTypedElement.getType(), java.util.Collection.class);
    }

    /**
     * Checks if the variable can be mutated.
     * <p>
     * Because we do not want to lint when people store an immutable copy via List.of() or similar.
     *
     * @param ctVariable the variable to check
     * @return true if the variable can be mutated, false otherwise
     */
    private static boolean canBeMutated(CtField<?> ctVariable) {
        // arrays are always mutable, there is no immutable array
        if (ctVariable.getType().isArray()) {
            return true;
        }

        if (!TypeUtil.isSubtypeOf(ctVariable.getType(), java.util.Collection.class)) {
            // not a collection
            return false;
        }

        // if the variable has a default value that is mutable, the variable is mutable
        CtExpression<?> defaultExpression = ctVariable.getDefaultExpression();
        if (defaultExpression != null
            && !defaultExpression.isImplicit()
            && isMutableExpression(defaultExpression)) {
            return true;
        }

        // TODO: we should only search for these in the constructor?
        // we check if there is a write to the variable with a value that is guaranteed to be mutable
        return UsesFinder.variableWrites(ctVariable)
            .hasAnyMatch(ctFieldWrite -> ctFieldWrite.getParent() instanceof CtAssignment<?, ?> ctAssignment
                && isMutableExpression(ctAssignment.getAssignment()));
    }

    /**
     * Checks if the state of the given expression can be mutated.
     *
     * @param ctExpression the expression to check
     * @return true if the expression is mutable, false otherwise
     */
    private static boolean isMutableExpression(CtExpression<?> ctExpression) {
        if (ctExpression instanceof CtNewArray<?>) {
            return true;
        }

        // we only care about arrays and collections for now
        if (!TypeUtil.isSubtypeOf(ctExpression.getType(), java.util.Collection.class)) {
            // not a collection
            return false;
        }

        // something like new ArrayList<>() is always mutable
        if (ctExpression instanceof CtConstructorCall<?>) {
            return true;
        }

        CtExecutable<?> ctExecutable = ctExpression.getParent(CtExecutable.class);

        // Sometimes we have this
        //
        // ```
        // List<String> values = new ArrayList<>();
        // values.add("foo");
        // this.values = values;
        // ```
        //
        // or this
        //
        // ```
        // public Constructor(List<String> values) {
        //     this.values = values;
        // }
        // ```
        //
        // In both cases the assigned expression is mutable, and we have to detect this.
        // To do this, we check if the assigned variable is mutable.
        if (ctExpression instanceof CtVariableRead<?> ctVariableRead && ctExecutable != null) {
            CtVariable<?> ctVariable = SpoonUtil.getVariableDeclaration(ctVariableRead.getVariable());

            // this is a special case for enums, where the constructor is private and mutability can only be introduced
            // through the enum constructor calls
            if (ctExecutable instanceof CtConstructor<?> ctConstructor
                && ctConstructor.getDeclaringType() instanceof CtEnum<?> ctEnum
                && hasAssignedParameterReference(ctExpression, ctConstructor)) {
                // figure out the index of the parameter reference:
                CtParameter<?> ctParameterToFind = findParameterReference(ctExpression, ctConstructor).orElseThrow();
                int index = -1;
                for (CtParameter<?> ctParameter : ctConstructor.getParameters()) {
                    index += 1;
                    if (ctParameter == ctParameterToFind) {
                        break;
                    }
                }

                if (index >= ctConstructor.getParameters().size() || index == -1) {
                    throw new IllegalStateException("Could not find parameter reference of %s in %s".formatted(ctExpression, ctConstructor));
                }

                for (CtEnumValue<?> ctEnumValue : ctEnum.getEnumValues()) {
                    if (ctEnumValue.getDefaultExpression() instanceof CtConstructorCall<?> ctConstructorCall
                        && ctConstructorCall.getExecutable().getExecutableDeclaration() == ctConstructor
                        && isMutableExpression(ctConstructorCall.getArguments().get(index))) {
                        return true;
                    }
                }

                return false;
            }

            if (hasAssignedParameterReference(ctVariableRead, ctExecutable) || (ctVariable.getDefaultExpression() != null && isMutableExpression(ctVariable.getDefaultExpression()))) {
                return true;
            }

            // the variable is mutable if it is assigned a mutable value in the declaration or through any write
            return UsesFinder.variableWrites(ctVariable)
                .hasAnyMatch(ctVariableWrite -> ctVariableWrite.getParent() instanceof CtAssignment<?,?> ctAssignment
                    && isMutableExpression(ctAssignment.getAssignment()));
        }

        return false;
    }

    private static boolean isParameterOf(CtVariable<?> ctVariable, CtExecutable<?> ctExecutable) {
        return ctExecutable.getParameters().stream().anyMatch(ctParameter -> ctParameter == ctVariable);
    }

    private static List<CtExpression<?>> findPreviousAssignee(CtVariableRead<?> ctVariableRead) {
        List<CtExpression<?>> result = new ArrayList<>();
        CtExecutable<?> ctExecutable = ctVariableRead.getParent(CtExecutable.class);

        boolean foundPreviousAssignment = false;
        CtStatement currentStatement = ctVariableRead.getParent(CtStatement.class);
        var reversedStatements = new ArrayList<>(SpoonUtil.getEffectiveStatements(ctExecutable.getBody()));
        Collections.reverse(reversedStatements);
        for (CtStatement ctStatement : reversedStatements) {
            if (!foundPreviousAssignment) {
                if (ctStatement == currentStatement) {
                    foundPreviousAssignment = true;
                }

                continue;
            }

            if (ctStatement instanceof CtAssignment<?,?> ctAssignment
                && ctAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite
                && ctVariableWrite.getVariable().equals(ctVariableRead.getVariable())) {

                result.add(ctAssignment.getAssignment());
            }
        }

        return result;
    }

    private static Option<CtParameter<?>> findParameterReference(CtExpression<?> ctExpression, CtExecutable<?> ctExecutable) {
        if (!(ctExpression instanceof CtVariableRead<?> ctVariableRead)) {
            return Option.none();
        }

        CtVariable<?> ctVariableDeclaration = SpoonUtil.getVariableDeclaration(ctVariableRead.getVariable());
        if (ctVariableDeclaration != null
            && isParameterOf(ctVariableDeclaration, ctExecutable)) {
            // There is a special-case: one can reassign the parameter to itself with a different value:
            //
            // values = List.copyOf(values);
            // this.values = values;
            //
            // Here it is guaranteed that the field is not mutably assigned.

            // TODO: replace recursion with a loop
            List<CtExpression<?>> previousAssignees = findPreviousAssignee(ctVariableRead);

            if (!previousAssignees.isEmpty()) {
                return findParameterReference(previousAssignees.get(0), ctExecutable);
            }

            return Option.some((CtParameter<?>) ctVariableDeclaration);
        }

        return Option.none();
    }

    private static boolean hasAssignedParameterReference(CtExpression<?> ctExpression, CtExecutable<?> ctExecutable) {
        return findParameterReference(ctExpression, ctExecutable).isSome();
    }

    private void checkCtExecutableReturn(CtExecutable<?> ctExecutable) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctExecutable.getBody());

        // a lambda like () -> true does not have a body, but an expression which is a return statement
        // this case is handled here
        if (statements.isEmpty() && ctExecutable instanceof CtLambda<?> ctLambda) {
            statements = List.of(createCtReturn(ctLambda.getExpression().clone()));
        }

        if (statements.isEmpty()
            // we should not check private methods (for those it should be okay to return a not-copied collection)
            || (ctExecutable instanceof CtModifiable ctModifiable && ctModifiable.isPrivate())) {
            return;
        }

        List<CtReturn> returns = statements.stream().flatMap(ctStatement -> {
            if (ctStatement instanceof CtReturn<?> ctReturn) {
                return List.of(ctReturn).stream();
            } else {
                return ctStatement.filterChildren(new TypeFilter<>(CtReturn.class)).list(CtReturn.class).stream();
            }
        }).toList();

        for (CtReturn<?> ctReturn : returns) {
            CtExpression<?> returnedExpression = ctReturn.getReturnedExpression();
            if (!(returnedExpression instanceof CtFieldRead<?> ctFieldRead)) {
                continue;
            }

            CtField<?> field = ctFieldRead.getVariable().getFieldDeclaration();

            // if the field is not private, it can be mutated anyway.
            if (field == null || !field.isPrivate()) {
                continue;
            }

            if (canBeMutated(field)) {
                addLocalProblem(
                    ElementUtil.findValidPosition(ctExecutable),
                    new LocalizedMessage(
                        "leaked-collection-return",
                        Map.of(
                            "method", ctExecutable.getSimpleName(),
                            "field", field.getSimpleName()
                        )
                    ),
                    ProblemType.LEAKED_COLLECTION_RETURN
                );
            }
        }
    }
    private static String formatSignature(CtExecutable<?> ctExecutable) {
        String name = ctExecutable.getSimpleName();
        if (ctExecutable instanceof CtConstructor<?> ctConstructor) {
            name = ctConstructor.getType().getSimpleName();
        }
        return "%s(%s)".formatted(
            name,
            ctExecutable.getParameters().stream().map(CtTypedElement::getType).map(CtTypeReference::toString).collect(Collectors.joining(", "))
        );
    }

    /**
     * Checks if the executable has an assignment to a field that is assigned a parameter reference,
     * then it could be mutated from the outside.
     *
     * @param ctExecutable the executable to check
     */
    private void checkCtExecutableAssign(CtExecutable<?> ctExecutable) {
        if (ctExecutable instanceof CtModifiable ctModifiable && ctModifiable.isPrivate()) {
            return;
        }

        for (CtStatement ctStatement : SpoonUtil.getEffectiveStatements(ctExecutable.getBody())) {
            if (!(ctStatement instanceof CtAssignment<?, ?> ctAssignment)
                || !(ctAssignment.getAssigned() instanceof CtFieldWrite<?> ctFieldWrite)) {
                continue;
            }

            CtField<?> ctField = ctFieldWrite.getVariable().getFieldDeclaration();

            if (hasAssignedParameterReference(ctAssignment.getAssignment(), ctExecutable)
                && ctField.isPrivate()
                // directly assigning the parameter is only a problem if the field can be mutated from the outside
                && isMutableType(ctField)) {
                if (ctExecutable instanceof CtConstructor<?> ctConstructor) {
                    addLocalProblem(
                        ElementUtil.findValidPosition(ctStatement),
                        new LocalizedMessage(
                            "leaked-collection-constructor",
                            Map.of(
                                "signature", formatSignature(ctConstructor),
                                "field", ctFieldWrite.getVariable().getSimpleName()
                            )
                        ),
                        ProblemType.LEAKED_COLLECTION_ASSIGN
                    );
                } else {
                    addLocalProblem(
                        ElementUtil.findValidPosition(ctStatement),
                        new LocalizedMessage(
                            "leaked-collection-assign",
                            Map.of(
                                "method", ctExecutable.getSimpleName(),
                                "field", ctFieldWrite.getVariable().getSimpleName()
                            )
                        ),
                        ProblemType.LEAKED_COLLECTION_ASSIGN
                    );
                }
            }
        }
    }

    private static CtReturn<?> createCtReturn(CtExpression<?> ctExpression) {
        CtReturn<?> ctReturn = ctExpression.getFactory().createReturn();
        return ctReturn.setReturnedExpression((CtExpression) ctExpression);
    }

    // The generated accessor method of a record does not have a real return statement.
    // This method fixes that by creating the return statement.
    private static CtMethod<?> fixRecordAccessor(CtRecord ctRecord, CtMethod<?> ctMethod) {
        // TODO: remove when https://github.com/INRIA/spoon/pull/5801 is merged.
        Factory factory = ctMethod.getFactory();
        CtMethod<?> result = ctMethod.clone();
        CtFieldRead ctFieldRead = factory.createFieldRead();

        ctFieldRead.setTarget(null);
        ctFieldRead.setVariable(ctRecord.getField(ctMethod.getSimpleName()).getReference());
        ctFieldRead.setType(result.getType());

        result.setBody(createCtReturn(ctFieldRead));
        result.setParent(ctRecord);

        return result;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            private <T> void checkCtType(CtType<T> ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) {
                    return;
                }

                for (CtTypeMember ctTypeMember : ctType.getTypeMembers()) {
                    if (ctType instanceof CtRecord ctRecord && ctTypeMember instanceof CtMethod<?> ctMethod && ctMethod.isImplicit()) {
                        ctTypeMember = fixRecordAccessor(ctRecord, ctMethod);
                    }

                    if (ctTypeMember instanceof CtConstructor<?> ctConstructor) {
                        checkCtExecutableAssign(ctConstructor);
                    } else if (ctTypeMember instanceof CtMethod<?> ctMethod) {
                        checkCtExecutableReturn(ctMethod);
                        checkCtExecutableAssign(ctMethod);
                    }
                }
            }

            // CtType has the following subtypes:
            // - CtClass
            // - CtEnum
            // - CtRecord
            //
            // - CtAnnotationType (not relevant)
            // - CtInterface (not relevant)
            // - CtTypeParameter (not relevant)

            @Override
            public <T> void visitCtClass(CtClass<T> ctClass) {
                this.checkCtType(ctClass);

                super.visitCtClass(ctClass);
            }

            @Override
            public <E extends Enum<?>> void visitCtEnum(CtEnum<E> ctEnum) {
                this.checkCtType(ctEnum);

                super.visitCtEnum(ctEnum);
            }

            @Override
            public void visitCtRecord(CtRecord ctRecord) {
                this.checkCtType(ctRecord);

                super.visitCtRecord(ctRecord);
            }

            @Override
            public <T> void visitCtLambda(CtLambda<T> ctLambda) {
                checkCtExecutableReturn(ctLambda);
                checkCtExecutableAssign(ctLambda);

                super.visitCtLambda(ctLambda);
            }

            @Override
            public void visitCtAnonymousExecutable(CtAnonymousExecutable ctAnonymousExecutable) {
                checkCtExecutableReturn(ctAnonymousExecutable);
                checkCtExecutableAssign(ctAnonymousExecutable);

                super.visitCtAnonymousExecutable(ctAnonymousExecutable);
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
