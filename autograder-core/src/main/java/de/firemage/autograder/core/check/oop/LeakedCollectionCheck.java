package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

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

@ExecutableCheck(reportedProblems = {
    ProblemType.LEAKED_COLLECTION_RETURN,
    ProblemType.LEAKED_COLLECTION_ASSIGN
})
public class LeakedCollectionCheck extends IntegratedCheck {
    private void checkCtExecutableReturn(CtExecutable<?> ctExecutable) {
        // parent will be null, if the return is in a constructor.
        // constructors are not considered methods.
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctExecutable.getBody());

        if (statements.isEmpty() && ctExecutable instanceof CtLambda<?> ctLambda) {
            statements = List.of(createCtReturn(ctLambda.getExpression().clone()));
        }

        if (statements.isEmpty()
            || (ctExecutable instanceof CtModifiable ctModifiable && ctModifiable.isPrivate())
            || !(statements.getLast() instanceof CtReturn<?> ctReturn)
            || ctReturn.getReturnedExpression() == null) {
            return;
        }

        CtExpression<?> returnedExpression = ctReturn.getReturnedExpression();

        if (!(returnedExpression instanceof CtFieldRead<?> ctFieldRead)) {
            return;
        }
        CtField<?> field = ctFieldRead.getVariable().getFieldDeclaration();

        if (canBeMutated(ctFieldRead) && field.isPrivate()) {
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

    private void checkCtExecutableAssign(CtExecutable<?> ctExecutable) {
        if (ctExecutable instanceof CtModifiable ctModifiable && ctModifiable.isPrivate()) {
            return;
        }

        for (CtStatement ctStatement : SpoonUtil.getEffectiveStatements(ctExecutable.getBody())) {
            if (!(ctStatement instanceof CtAssignment<?,?> ctAssignment)
                || !(ctAssignment.getAssigned() instanceof CtFieldWrite<?> ctFieldWrite)) {
                continue;
            }

            if (hasAssignedParameterReference(ctAssignment.getAssignment(), ctExecutable)
                && ctFieldWrite.getVariable().getFieldDeclaration().isPrivate()
                && canBeMutated(ctFieldWrite)) {
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
        CtBlock<?> ctBody = factory.createBlock();
        CtFieldRead ctFieldRead = factory.createFieldRead();

        ctFieldRead.setTarget(null);
        ctFieldRead.setVariable(ctRecord.getField(ctMethod.getSimpleName()).getReference());
        ctFieldRead.setType(result.getType());

        ctBody.setStatements(List.of(createCtReturn(ctFieldRead)));
        result.setBody(ctBody);
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

    // checks if the variable can be mutated from the outside
    private static boolean canBeMutated(CtFieldAccess<?> ctFieldAccess) {
        CtFieldReference<?> ctFieldReference = ctFieldAccess.getVariable();
        CtField<?> ctField = ctFieldReference.getFieldDeclaration();

        // arrays are always mutable, there is no immutable array
        if (ctField.getType().isArray()) {
            return true;
        }

        if (!SpoonUtil.isSubtypeOf(ctField.getType(), java.util.Collection.class)) {
            // not a collection
            return false;
        }

        // there are some special classes that are always immutable
        // for those we check if they were assigned that special construct
        //
        // (e.g. List.of(), Set.of(), Map.of(), Collections.unmodifiableList(), ...)

        // we check if there is a write to the field with a value that is guranteed to be mutable
        return SpoonUtil.hasAnyUses(ctField, ctElement -> ctElement instanceof CtFieldWrite<?> ctVariableWrite
            && ctVariableWrite.getVariable().equals(ctFieldReference)
            && ctVariableWrite.getParent() instanceof CtAssignment<?,?> ctAssignment
            && ctAssignment.getAssigned() == ctVariableWrite
            && isMutableAssignee(ctAssignment.getAssignment()))
            || (ctField.getAssignment() != null && isMutableAssignee(ctField.getAssignment()));
    }

    private static boolean isMutableAssignee(CtExpression<?> ctExpression) {
        if (ctExpression instanceof CtNewArray<?>) {
            return true;
        }

        if (ctExpression instanceof CtConstructorCall<?>) {
            return true;
        }

        // if a public method assigns a parameter to a field that parameter can be mutated from the outside
        // => the field is mutable assignee
        CtExecutable<?> ctExecutable = ctExpression.getParent(CtExecutable.class);
        return ctExecutable instanceof CtModifiable ctModifiable
            && !ctModifiable.isPrivate()
            && hasAssignedParameterReference(ctExpression, ctExecutable);
    }

    private static boolean hasAssignedParameterReference(CtExpression<?> ctExpression, CtExecutable<?> ctExecutable) {
        return ctExpression instanceof CtVariableRead<?> ctVariableRead
            && isParameterOf(ctVariableRead.getVariable(), ctExecutable);
    }

    private static boolean isParameterOf(CtVariableReference<?> ctVariableReference, CtExecutable<?> ctExecutable) {
        CtElement declaration = SpoonUtil.getReferenceDeclaration(ctVariableReference);
        if (declaration == null) {
            return false;
        }

        return ctExecutable.getParameters().stream().anyMatch(ctParameter -> ctParameter == declaration);
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
