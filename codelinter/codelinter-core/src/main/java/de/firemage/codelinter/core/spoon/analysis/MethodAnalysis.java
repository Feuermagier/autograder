package de.firemage.codelinter.core.spoon.analysis;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

import java.util.HashMap;
import java.util.Map;

public class MethodAnalysis {
    private final CtModel model;
    private final Map<CtMethod<?>, MethodAnalysisResult> resultCache;

    public MethodAnalysis(CtModel model) {
        this.model = model;
        this.resultCache = new HashMap<>();
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
        MethodAnalysisResult result = new MethodAnalysisResult(method.getType());
        var variableState = new HashMap<CtVariable<?>, VariableState>();
        analyzeBlock(method.getBody(), variableState, result);
        return result;
    }

    private StatementControlFlowResult analyzeBlock(CtBlock<?> block, Map<CtVariable<?>, VariableState> variableState,
                              MethodAnalysisResult result) {
        for (var statement : block.getStatements()) {
            var statementResult = analyzeStatement(statement, variableState, result);
            if (statementResult instanceof ReturnedStatementResult || statementResult instanceof ExceptionStatementResult) {
                return statementResult;
            }
        }
        return new NonBreakingStatementResult();
    }

    private StatementControlFlowResult analyzeStatement(CtStatement statement, Map<CtVariable<?>, VariableState> variableState, MethodAnalysisResult result) {
        if (statement instanceof CtReturn<?> ret) {
            ExpressionResult state = analyzeExpression(ret.getReturnedExpression(), variableState);
            if (state.isException()) {
                return new ExceptionStatementResult(state.getThrownException());
            } else {
                result.addReturnValue(state.getResult());
                return new ReturnedStatementResult(state.getResult());
            }
        } else if (statement instanceof CtLocalVariable<?> local) {
            if (local.getAssignment() != null) {
                ExpressionResult state = analyzeExpression(local.getAssignment(), variableState);
                if (state.isException()) {
                    return new ExceptionStatementResult(state.getThrownException());
                } else {
                    variableState.put(local, state.getResult());
                }
            } else {
                variableState.put(local, VariableState.defaultForType(local.getType()));
            }
            return new NonBreakingStatementResult();
        } else if (statement instanceof CtAssignment<?, ?> assignment) {
            analyzeExpression(assignment, variableState);
            return new NonBreakingStatementResult();
        } else if (statement instanceof CtIf ifStmt) {
            ExpressionResult conditionState = analyzeExpression(ifStmt.getCondition(), variableState);
            if (conditionState.isException()) {
                return new ExceptionStatementResult(conditionState.getThrownException());
            }
            if (conditionState.getResult().getSuperset() instanceof BooleanValueSet superset) {
                var ifState = copyVariableState(variableState);
                if (superset.containsTrue()) {
                    analyzeStatement(ifStmt.getThenStatement(), ifState, result);
                }
                if (superset.containsFalse() && ifStmt.getElseStatement() != null) {
                    var elseState = copyVariableState(variableState);
                    analyzeStatement(ifStmt.getElseStatement(), elseState, result);
                    includeBranch(ifState, elseState);
                }
                // Hacky, but it works
                variableState.clear();
                variableState.putAll(ifState);
                return new NonBreakingStatementResult(); // TODO use the information from the branches
            } else {
                throw new IllegalStateException();
            }

        } else if (statement instanceof CtBlock<?> innerBlock) {
            return analyzeBlock(innerBlock, variableState, result);
        } else {
            throw new IllegalStateException();
        }
    }

    // Returns true if the expression is inferred to be nullable
    private ExpressionResult analyzeExpression(CtExpression<?> expr, Map<CtVariable<?>, VariableState> variableState) {
        if (expr instanceof CtAssignment<?, ?> assignment) {
            ExpressionResult rhsNullable = analyzeExpression(assignment.getAssignment(), variableState);
            if (rhsNullable.isException()) {
                return rhsNullable;
            }
            analyzeAssignLhs(assignment.getAssigned(), rhsNullable.getResult(), variableState);
            return rhsNullable;
        } else if (expr instanceof CtVariableRead<?> read) {
            CtVariable<?> variable = read.getVariable().getDeclaration();
            return new ExpressionResult(variableState.getOrDefault(variable, VariableState.defaultForType(expr.getType())));
        } else if (expr instanceof CtLiteral<?> literal) {
            return new ExpressionResult(literalToVariableState(literal));
        } else if (expr instanceof CtInvocation<?> invocation) {
            return analyzeInvocation(invocation, variableState);
        } else if (expr instanceof CtConstructorCall<?>) {
            return new ExpressionResult(new VariableState(VariableType.REFERENCE, new ObjectValueSet(false, true)));
        } else if (expr instanceof CtUnaryOperator<?> operator) {
            ExpressionResult inner = analyzeExpression(operator.getOperand(), variableState);
            if (inner.isException()) {
                return inner;
            }
            return new ExpressionResult(inner.getResult().handleUnaryOperator(operator.getKind()));
        } else if (expr instanceof CtBinaryOperator<?> operator) {
            ExpressionResult lhs = analyzeExpression(operator.getLeftHandOperand(), variableState);
            if (lhs.isException()) {
                return lhs;
            }
            ExpressionResult rhs = analyzeExpression(operator.getRightHandOperand(), variableState);
            if (rhs.isException()) {
                return rhs;
            }
            return new ExpressionResult(lhs.getResult().handleBinaryOperator(operator.getKind(), rhs.getResult()));
        } else {
            throw new UnsupportedOperationException(expr.getClass().toString());
        }
    }

    private void analyzeAssignLhs(CtExpression<?> lhs, VariableState rhs,
                                  Map<CtVariable<?>, VariableState> variableState) {
        if (lhs instanceof CtVariableWrite<?> write) {
            CtVariable<?> variable = write.getVariable().getDeclaration();
            if (isValidTarget(variable)) {
                variableState.put(variable, rhs);
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

    private ExpressionResult analyzeInvocation(CtInvocation<?> invocation, Map<CtVariable<?>, VariableState> variableState) {
        CtExpression<?> target = invocation.getTarget();
        ExpressionResult targetState = analyzeExpression(target, variableState);
        if (targetState.isException()) {
            // If the callee expression throws, we throw
            return targetState;
        } else if (targetState.getResult().getSuperset() instanceof ObjectValueSet values) {
            // If the callee expression must be null, we throw
            if (values.containsNull() && !values.containsInstance()) {
                return new ExpressionResult(new ThrownException("java.lang.NullPointerException", invocation));
            }

            // Now check the called method and analyze the returned value
            CtExecutable<?> executable = invocation.getExecutable().getExecutableDeclaration();
            if (executable instanceof CtMethod<?> method) {
                return new ExpressionResult(this.resultCache.computeIfAbsent(method, this::analyzeMethod).getReturnState());
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private Map<CtVariable<?>, VariableState> copyVariableState(Map<CtVariable<?>, VariableState> variableState) {
        Map<CtVariable<?>, VariableState> copy = new HashMap<>();
        for (Map.Entry<CtVariable<?>, VariableState> entry : variableState.entrySet()) {
            copy.put(entry.getKey(), new VariableState(entry.getValue()));
        }
        return copy;
    }

    private void includeBranch(Map<CtVariable<?>, VariableState> target, Map<CtVariable<?>, VariableState> other) {
        for (Map.Entry<CtVariable<?>, VariableState> otherEntry : other.entrySet()) {
            if (target.containsKey(otherEntry.getKey())) {
                target.put(otherEntry.getKey(), new VariableState(target.get(otherEntry.getKey()), otherEntry.getValue()));
            }
        }
    }

    private VariableState literalToVariableState(CtLiteral<?> literal) {
        if (literal.getValue() == null) {
            ValueSet valueSet = new ObjectValueSet(true, false);
            return new VariableState(VariableType.REFERENCE, valueSet);
        } else if (literal.getValue().equals(Boolean.TRUE)) {
            ValueSet valueSet = new BooleanValueSet(true, false);
            return new VariableState(VariableType.BOOLEAN, valueSet);
        } else if (literal.getValue().equals(Boolean.FALSE)) {
            ValueSet valueSet = new BooleanValueSet(false, true);
            return new VariableState(VariableType.BOOLEAN, valueSet);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
