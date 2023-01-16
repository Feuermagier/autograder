package de.firemage.autograder.core.integrated.scope;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.scope.value.ArrayValue;
import de.firemage.autograder.core.integrated.scope.value.UnknownValue;
import de.firemage.autograder.core.integrated.scope.value.Value;
import de.firemage.autograder.core.integrated.scope.value.VariableValue;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Scope {
    private final List<Map<CtVariableReference<?>, Value>> variables;

    Scope() {
        this.variables = new ArrayList<>();
        this.variables.add(new HashMap<>());
    }

    private Value resolveArrayRead(CtArrayRead<?> ctArrayRead) {
        // first resolve the index into the array
        Value index = this.resolve(ctArrayRead.getIndexExpression());
        CtVariableReference<?> variable = SpoonUtil.getVariableFromArray(ctArrayRead).getVariable();

        Value storedValue = this.get(variable);

        // if the array is not known, return an unknown value
        if (storedValue == null) {
            return new UnknownValue();
        }

        if (!(storedValue instanceof ArrayValue arrayValue)) {
            throw new IllegalArgumentException("Variable " + variable + " is not stored as an array, but should be");
        }

        return arrayValue.get(index, this);
    }

    public Value resolve(CtExpression<?> value) {
        if (value instanceof CtVariableRead<?> access) {
            Value varValue = this.get(access.getVariable());
            if (varValue != null) {
                return varValue.toExpression().map(this::resolve).orElse(varValue);
            }
        } else if (value instanceof CtLiteral<?> literal) {
            return VariableValue.fromLiteral(literal);
        } else if (value instanceof CtNewArray<?> newArray) {
            return ArrayValue.fromNew(newArray);
        } else if (value instanceof CtArrayRead<?> ctArrayRead) {
            return this.resolveArrayRead(ctArrayRead);
        }

        return VariableValue.fromExpression(value);
    }

    /**
     * Registers the variable in the current scope having the specified value.
     *
     * @param variable the variable to register
     * @param value    the value it has
     */
    void register(CtVariableReference<?> variable, CtExpression<?> value) {
        this.register(variable, this.resolve(value));
    }

    private void register(CtVariableReference<?> variable, Value value) {
        this.variables.get(this.variables.size() - 1).put(variable, value);
    }

    void update(CtVariableReference<?> variable, CtExpression<?> value) {
        for (int i = this.variables.size() - 1; i >= 0; i--) {
            if (this.variables.get(i).containsKey(variable)) {
                this.variables.get(i).put(variable, this.resolve(value));
                return;
            }
        }

        throw new IllegalArgumentException("Variable " + variable + " is not registered in any scope");
    }

    void registerOrUpdateArray(CtArrayWrite<?> ctArrayWrite, CtExpression<?> value) {
        // first resolve the value assigned to the array:
        Value resolvedValue = this.resolve(value);

        // first get the name of the array:
        CtVariableReference<?> variableReference = SpoonUtil.getVariableFromArray(ctArrayWrite).getVariable();
        // then check if the array is already registered:
        Value storedValue = this.get(variableReference);

        Value index = VariableValue.fromExpression(ctArrayWrite.getIndexExpression());
        ArrayValue arrayValue = (ArrayValue) storedValue;

        if (storedValue != null) {
            // if the value is known, check that the index is known as well:
            if (!index.isConstant()) {
                // try to resolve the index:
                index = index.toExpression().map(this::resolve).orElse(index);
                if (!index.isConstant()) {
                    // if the index is not known, it can be anything, so we need to invalidate the whole array:
                    arrayValue.invalidate();
                }
            }

            // if it is, then update the value at the specified index:
            arrayValue.set(index, resolvedValue);
        } else {
            // the array has not yet been registered. It is not known what value each index has, so
            // it is an unknown array value.
            ArrayValue newValue = ArrayValue.unknown();
            // update the value at the specified index (which is known):
            newValue.set(index, resolvedValue);

            this.register(variableReference, newValue);
        }

    }

    void updateOrRegister(CtVariableReference<?> variable, CtExpression<?> value) {
        try {
            this.update(variable, value);
        } catch (IllegalArgumentException e) {
            this.register(variable, value);
        }
    }

    /**
     * Returns the value of the variable or null if it is not registered.
     *
     * @param variable the variable of which one should get the value
     *
     * @return the value or null if it is unknown.
     */
    public Value get(CtVariableReference<?> variable) {
        List<Map<CtVariableReference<?>, Value>> reversedVars = new ArrayList<>(this.variables);
        Collections.reverse(reversedVars);

        for (var scope : reversedVars) {
            if (scope.containsKey(variable)) {
                return scope.get(variable);
            }
        }

        return null;
    }

    public void remove(CtVariableReference<?> ctVariableReference) {
        var reversedVars = new ArrayList<>(this.variables);
        Collections.reverse(reversedVars);

        for (var scope : reversedVars) {
            var v = scope.remove(ctVariableReference);
            if (v != null) return;
        }
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
