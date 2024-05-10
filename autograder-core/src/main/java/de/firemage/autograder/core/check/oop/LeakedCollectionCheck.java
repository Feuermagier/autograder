package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    /**
     * Checks if the field can be mutated (if it is not okay to return a reference to it without copying it).
     * <p>
     * Because we do not want to lint when people store an immutable copy via List.of() or similar.
     *
     * @param ctField the field to check
     * @return true if the field can be mutated, false otherwise
     */
    private static boolean canBeMutated(CtField<?> ctField) {
        // arrays are always mutable, there is no immutable array
        if (ctField.getType().isArray()) {
            return true;
        }

        if (!SpoonUtil.isSubtypeOf(ctField.getType(), java.util.Collection.class)) {
            // not a collection
            return false;
        }

        // TODO: what is this supposed to be? I don't understand why this function exists

        // TODO: what if the default value is always overwritten in the constructor? Then it is never mutable?

        // if the field has a default value that is mutable, the field is mutable
        if (ctField.getAssignment() != null && isMutableAssignee(ctField.getAssignment())) {
            return true;
        }

        // we check if there is a write to the field with a value that is guaranteed to be mutable
        return UsesFinder.variableUses(ctField)
                .ofType(CtVariableWrite.class)
                .filterDirectParent(CtAssignment.class, a -> isMutableAssignee(a.getAssignment()))
                .hasAny();
    }

    /**
     * Checks if the given expression is mutable (it would be problematic to return a reference to it without copying it).
     *
     * @param ctExpression the expression to check
     * @return true if the expression is mutable, false otherwise
     */
    private static boolean isMutableAssignee(CtExpression<?> ctExpression) {
        if (ctExpression instanceof CtNewArray<?>) {
            return true;
        }

        if (!SpoonUtil.isSubtypeOf(ctExpression.getType(), java.util.Collection.class)) {
            // not a collection
            return false;
        }

        // something like new ArrayList<>() is always mutable
        if (ctExpression instanceof CtConstructorCall<?>) {
            return true;
        }

        CtExecutable<?> ctExecutable = ctExpression.getParent(CtExecutable.class);

        // if a variable is assigned to the field, one has to check if the variable is mutable
        if (ctExpression instanceof CtVariableRead<?> ctVariableRead) {
            CtVariable<?> ctVariable = SpoonUtil.getVariableDeclaration(ctVariableRead.getVariable());

            // the variable is mutable if it is assigned a mutable value in the declaration or through any write
            return (ctVariable.getDefaultExpression() != null && isMutableAssignee(ctVariable.getDefaultExpression()))
                || UsesFinder.variableUses(ctVariable)
                    .ofType(CtVariableWrite.class)
                    .filterDirectParent(CtAssignment.class, a -> isMutableAssignee(a.getAssignment()))
                    .hasAny();
        }

        // if a public method assigns a parameter to a field that parameter can be mutated from the outside
        // => the field is mutable assignee
        if (ctExecutable instanceof CtModifiable ctModifiable
            && !ctModifiable.isPrivate()
            && hasAssignedParameterReference(ctExpression, ctExecutable)) {
            return true;
        }

        return false;
    }

    private static boolean isParameterOf(CtVariableReference<?> ctVariableReference, CtExecutable<?> ctExecutable) {
        CtElement declaration = SpoonUtil.getReferenceDeclaration(ctVariableReference);
        if (declaration == null) {
            return false;
        }

        return ctExecutable.getParameters().stream().anyMatch(ctParameter -> ctParameter == declaration);
    }

    private static List<CtExpression<?>> findPreviousAssignee(CtVariableRead<?> ctVariableRead) {
        List<CtExpression<?>> result = new ArrayList<>();
        CtExecutable<?> ctExecutable = ctVariableRead.getParent(CtExecutable.class);

        boolean foundPreviousAssignment = false;
        CtStatement currentStatement = ctVariableRead.getParent(CtStatement.class);
        for (CtStatement ctStatement : SpoonUtil.getEffectiveStatements(ctExecutable.getBody()).reversed()) {
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

    private static boolean hasAssignedParameterReference(CtExpression<?> ctExpression, CtExecutable<?> ctExecutable) {
        if (ctExpression instanceof CtVariableRead<?> ctVariableRead && isParameterOf(ctVariableRead.getVariable(), ctExecutable)) {
            // There is a special-case: one can reassign the parameter to itself with a different value:
            //
            // values = List.copyOf(values);
            // this.values = values;
            //
            // Here it is guaranteed that the field is not mutably assigned.

            List<CtExpression<?>> previousAssignees = findPreviousAssignee(ctVariableRead);

            if (!previousAssignees.isEmpty()) {
                return hasAssignedParameterReference(previousAssignees.getFirst(), ctExecutable);
            }

            return true;
        }

        return false;
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
            if (!field.isPrivate()) {
                continue;
            }

            if (canBeMutated(field)) {
                addLocalProblem(
                    SpoonUtil.findValidPosition(ctExecutable),
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

            if (hasAssignedParameterReference(ctAssignment.getAssignment(), ctExecutable)
                && ctFieldWrite.getVariable().getFieldDeclaration().isPrivate()) {
                String methodName = ctExecutable.getSimpleName();
                if (methodName.equals("<init>")) {
                    methodName = ctExecutable.getParent(CtType.class).getSimpleName();
                }

                addLocalProblem(
                    SpoonUtil.findValidPosition(ctStatement),
                    new LocalizedMessage(
                        "leaked-collection-assign",
                        Map.of(
                            "method", methodName,
                            "field", ctFieldWrite.getVariable().getSimpleName()
                        )
                    ),
                    ProblemType.LEAKED_COLLECTION_ASSIGN
                );
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
            @Override
            public void visitCtRecord(CtRecord ctRecord) {
                for (CtMethod<?> ctMethod : ctRecord.getMethods()) {
                    if (ctMethod.isImplicit()) {
                        this.visitCtMethod(fixRecordAccessor(ctRecord, ctMethod));
                    }
                }

                super.visitCtRecord(ctRecord);
            }

            @Override
            public <T> void visitCtConstructor(CtConstructor<T> ctConstructor) {
                checkCtExecutableReturn(ctConstructor);
                checkCtExecutableAssign(ctConstructor);

                super.visitCtConstructor(ctConstructor);
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                checkCtExecutableReturn(ctMethod);
                checkCtExecutableAssign(ctMethod);

                super.visitCtMethod(ctMethod);
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
