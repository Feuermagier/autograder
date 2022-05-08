package de.firemage.codelinter.core.spoon.analysis;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import java.util.HashMap;
import java.util.Map;

public class MethodAnalysis {
    private final CtModel model;

    public MethodAnalysis(CtModel model) {
        this.model = model;
    }

    public void run() {
        var methods = this.model.getElements(e -> e instanceof CtMethod<?>);
        var results = methods.stream()
                .map(e -> (CtMethod<?>) e)
                .map(this::analyzeMethod)
                .toList();
        results.forEach(System.out::println);
    }

    private MethodAnalysisResult analyzeMethod(CtMethod<?> method) {
        MethodAnalysisResult result = new MethodAnalysisResult();
        var nullableState = new HashMap<CtVariable<?>, Boolean>();
        analyzeBlock(method.getBody(), nullableState, result);
        return result;
    }

    private void analyzeBlock(CtBlock<?> block, Map<CtVariable<?>, Boolean> nullableState, MethodAnalysisResult result) {
        for (var statement : block.getStatements()) {
            if (statement instanceof CtReturn<?> ret) {
                if (processExpression(ret.getReturnedExpression(), nullableState)) {
                    result.markAsNullable();
                }
            } else if (statement instanceof CtLocalVariable<?> local) {
                if (local.getAssignment() != null) {
                    nullableState.put(local, processExpression(local.getAssignment(), nullableState));
                } else {
                    nullableState.put(local, false);
                }
            } else if (statement instanceof CtAssignment<?, ?> assignment) {
                processExpression(assignment, nullableState);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    // Returns true if the expression is inferred to be nullable
    private boolean processExpression(CtExpression<?> expr, Map<CtVariable<?>, Boolean> nullableState) {
        if (expr instanceof CtAssignment<?, ?> assignment) {
            boolean rhsNullable = processExpression(assignment.getAssignment(), nullableState);
            processAssignLhs(assignment.getAssigned(), rhsNullable, nullableState);
            return rhsNullable;
        } else if (expr instanceof CtVariableRead read) {
            CtVariable<?> variable = read.getVariable().getDeclaration();
            return nullableState.getOrDefault(variable, false);
        } else if (expr instanceof CtLiteral<?> literal) {
            if (literal.getValue() == null) {
                return true;
            } else {
                return false;
            }
        } else if (expr instanceof CtInvocation<?> invocation) {
            return false;
        } else if (expr instanceof CtConstructorCall<?>) {
            return false;
        } else {
            throw new IllegalStateException();
        }
    }

    private void processAssignLhs(CtExpression<?> lhs, boolean rhsNullable, Map<CtVariable<?>, Boolean> nullableState) {
        if (lhs instanceof CtVariableWrite<?> write) {
            CtVariable<?> variable = write.getVariable().getDeclaration();
            if (isValidTarget(variable)) {
                nullableState.put(variable, rhsNullable);
            }
        } else if (lhs instanceof CtArrayWrite<?>) {
            // Ignore arrays
        } else {
            throw new IllegalStateException();
        }
    }

    private boolean isValidTarget(CtVariable<?> variable) {
        return !(variable instanceof CtField<?>);
    }
}
