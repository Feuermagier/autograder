package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtLiteralImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scope {
    private final List<Map<CtVariableReference<?>, CtExpression<?>>> variables;

    Scope() {
        this.variables = new ArrayList<>();
        this.variables.add(new HashMap<>());
    }

    /**
     * Registers the variable in the current scope having the specified value.
     *
     * @param variable the variable to register
     * @param value the value it has
     */
    void register(CtVariableReference<?> variable, CtExpression<?> value) {
        this.variables.get(this.variables.size() - 1).put(variable, value);
    }

    /**
     * Removes the variable from the scope.
     *
     * @param variable the variable to remove.
     */
    void remove(CtVariableReference<?> variable) {
        this.variables.get(this.variables.size() - 1).remove(variable);
    }

    /**
     * Returns the value of the variable or null.
     *
     * @param variable the variable of which one should get the value
     * @return the value or null if it is unknown.
     */
    CtExpression<?> get(CtVariableReference<?> variable) {
        List<Map<CtVariableReference<?>, CtExpression<?>>> reversedVars = new ArrayList<>(this.variables);
        Collections.reverse(reversedVars);

        for (var scope : reversedVars) {
            if (scope.containsKey(variable)) {
                return scope.get(variable);
            }
        }

        return null;
    }

    /**
     * Adds a new (empty) scope to the stack.
     */
    void push() {
        this.variables.add(new HashMap<>());
    }

    /**
     * Removes the last scope from the stack.
     */
    void pop() {
        this.variables.remove(this.variables.size() - 1);
        if (this.variables.isEmpty()) {
            this.variables.add(new HashMap<>());
        }
    }
}

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_ARRAY_INIT })
public class RedundantArrayInit extends IntegratedCheck {
    private static final Map<String, CtLiteral<?>> DEFAULT_VALUES = Map.ofEntries(
        Map.entry("int", makeLiteral(0)),
        Map.entry("double", makeLiteral(0.0d)),
        Map.entry("float", makeLiteral(0.0f)),
        Map.entry("long", makeLiteral(0L)),
        Map.entry("short", makeLiteral((short) 0)),
        Map.entry("byte", makeLiteral((byte) 0)),
        Map.entry("char", makeLiteral((char) 0)),
        Map.entry("boolean", makeLiteral(false))
    );

    private static <T> CtLiteral<?> makeLiteral(T value) {
        CtLiteral<T> literal = new CtLiteralImpl<>();
        literal.setValue(value);
        return literal;
    }

    public RedundantArrayInit() {
        super(new LocalizedMessage("redundant-array-init-desc"));
    }

    private static <T> Integer integerValue(T value) {
        if (value instanceof Byte res) {
            return res.intValue();
        } else if (value instanceof Short res) {
            return res.intValue();
        } else if (value instanceof Character res) {
            return (int) res;
        } else if (value instanceof Integer res) {
            return res;
        } else {
            return null;
        }
    }

    // equals impl of CtLiteral seems to be broken
    private static boolean areLiteralsEqual(CtLiteral<?> left, CtLiteral<?> right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }

        if (left.getValue() == null) {
            return right.getValue() == null;
        } else if (right.getValue() == null) {
            return false;
        }

        // short, char and byte can be implicitly casted
        // so the literal in the code might be of type int, while the default value
        // has the correct type short, char or byte
        //
        // To solve this, try to cast.

        Object leftValue = left.getValue();
        Object rightValue = right.getValue();
        Integer leftIntValue = integerValue(leftValue);
        Integer rightIntValue = integerValue(rightValue);

        if (leftIntValue != null && rightIntValue != null) {
            return leftIntValue.equals(rightIntValue);
        }

        boolean result = true;

        // ensure that it will return true, when one base is not specified
        if (left.getBase() != null && right.getBase() != null) {
            result = left.getBase() == right.getBase();
        }

        return result && leftValue.equals(rightValue);
    }

    private static CtVariableAccess<?> getVariableFromArray(CtArrayAccess<?, ?> ctArrayAccess) {
        CtExpression<?> array = ctArrayAccess.getTarget();

        if (array instanceof CtVariableAccess<?>) {
            return (CtVariableAccess<?>) array;
        } else if (array instanceof CtArrayAccess<?, ?>) {
            return getVariableFromArray((CtArrayAccess<?, ?>) array);
        } else {
            return null;
        }
    }

    private <L, R> void checkAssignment(
        L lhs,
        R rhs,
        CtElement element,
        Scope scope
    ) {
        // check if a new array is assigned to the variable and then put it into the scope
        if (lhs instanceof CtVariableWrite<?> write) {
            CtLiteral<?> defaultValue = getDefaultArrayValueOrNull(write.getType(), rhs);
            if (defaultValue != null) {
                scope.register(write.getVariable(), defaultValue);
            }

            return;
        }

        if (lhs instanceof CtArrayWrite<?> assigned) {
            CtVariableAccess<?> read = getVariableFromArray(assigned);
            // could not access the variable, should not happen...
            if (read == null) {
                return;
            }
            CtVariableReference<?> variableName = read.getVariable();

            CtLiteral<?> defaultValue = (CtLiteral<?>) scope.get(variableName);
            if (defaultValue == null) {
                // could not find the default value
                return;
            }

            // check if the assigned value is a literal. If it is, then check for
            // equality with the respective default for each type.
            if (!(rhs instanceof CtLiteral<?> rhsValue)
                || !areLiteralsEqual(rhsValue, defaultValue)) {
                // a non-default value has been assigned to the array
                // therefore it is removed from the pool of default values
                scope.remove(variableName);
                return;
            }

            // the assigned value is the default value, so it can be removed.
            this.addLocalProblem(
                element,
                new LocalizedMessage("redundant-array-init-desc"),
                ProblemType.REDUNDANT_ARRAY_INIT
            );
        }
    }

    private void processStatement(CtElement statement, Scope scope) {
        if (statement instanceof CtAssignment<?, ?> assignment) {
            this.checkAssignment(assignment.getAssigned(), assignment.getAssignment(), statement, scope);
        }

        // check declaration:
        if (statement instanceof CtVariable<?> variable && statement instanceof CtRHSReceiver<?> rhs) {
            // check if a new array is assigned to the variable and then put it into the scope
            CtLiteral<?> defaultValue = getDefaultArrayValueOrNull(variable.getType(), rhs.getAssignment());
            if (defaultValue != null) {
                scope.register(variable.getReference(), defaultValue);
            }
        }

        if (statement instanceof CtBlock<?> innerBlock) {
            scope.push();

            processBlock(innerBlock, scope);

            scope.pop();
        }

        if (statement instanceof CtLoop loop) {
            scope.push();

            processStatement(loop.getBody(), scope);

            scope.pop();
        }

        if (statement instanceof CtIf conditional) {
            scope.push();

            processStatement(conditional.getThenStatement(), scope);
            processStatement(conditional.getElseStatement(), scope);

            scope.pop();
        }
    }

    private void processBlock(CtBlock<?> block, Scope scope) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(block);

        for (CtStatement statement : statements) {
            processStatement(statement, scope);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBlock<?>>() {
            @Override
            public void process(CtBlock<?> block) {
                Scope scope = new Scope();
                processBlock(block, scope);
            }
        });
    }

    private static <R> CtLiteral<?> getDefaultArrayValueOrNull(CtTypeInformation type, R rhs) {
        // skip all non-array assignments:
        if (!type.isArray()) {
            return null;
        }

        CtArrayTypeReference<?> arrayType = (CtArrayTypeReference<?>) type;

        // skip all non-default array assignments:
        if (!(rhs instanceof CtNewArray<?> newArray)) {
            return null;
        }

        if (!newArray.getElements().isEmpty()) {
            return null;
        }

        CtTypeReference<?> realType = arrayType.getArrayType();
        if (realType.isPrimitive() && !DEFAULT_VALUES.containsKey(realType.getSimpleName())) {
            // skip all types for which we don't have a default value
            return null;
        }

        if (realType.isPrimitive()) {
            return DEFAULT_VALUES.get(realType.getSimpleName());
        } else {
            return makeLiteral(null);
        }
    }
}
