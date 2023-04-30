package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.scope.Scope;
import de.firemage.autograder.core.integrated.scope.ScopedVisitor;
import de.firemage.autograder.core.integrated.scope.value.ArrayValue;
import de.firemage.autograder.core.integrated.scope.value.Value;
import de.firemage.autograder.core.integrated.scope.value.VariableValue;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;


// Check is bugged
@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_ARRAY_INIT }, enabled = false)
public class RedundantArrayInit extends IntegratedCheck {
    private <L, R> void checkAssignment(
        L lhs,
        CtExpression<R> rhs,
        CtElement element,
        Scope scope
    ) {
        if (lhs instanceof CtArrayWrite<?> arrayWrite) {
            CtVariableAccess<?> read = SpoonUtil.getVariableFromArray(arrayWrite);
            CtVariableReference<?> variableName = read.getVariable();

            // this should not happen:
            if (!(scope.get(variableName) instanceof ArrayValue arrayValue)) {
                throw new IllegalStateException("Expected array value for " + variableName);
            }

            Value currentValue = arrayValue.get(VariableValue.fromExpression(arrayWrite.getIndexExpression()), scope);

            if (currentValue == null) {
                return;
            }

            Value rhsValue = scope.resolve(rhs);

            currentValue.toLiteral().ifPresent(literal -> {
                CtLiteral<?> rhsLiteralValue = rhsValue.toLiteral().orElse(null);
                if (rhsLiteralValue != null && SpoonUtil.areLiteralsEqual(rhsLiteralValue, literal)) {
                    // the current literal value is the same as the rhsValue

                    // the assigned value is the default value, so it can be removed.
                    this.addLocalProblem(
                        element,
                        new LocalizedMessage("redundant-array-init"),
                        ProblemType.REDUNDANT_ARRAY_INIT
                    );
                }
            });
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        // TODO: is getModel().getRootPackage() correct?
        staticAnalysis.getModel().getRootPackage().accept(new ScopedVisitor() {
            @Override
            public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
                checkAssignment(assignment.getAssigned(), assignment.getAssignment(), assignment, this.getScope());

                super.visitCtAssignment(assignment);
            }
        });
    }
}
