package de.firemage.autograder.core.integrated.scope.value;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.scope.Scope;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.reference.CtArrayTypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ArrayValue implements Value {
    private Value defaultValue;
    private final Map<IndexValueWrapper, Value> values;

    private ArrayValue(Value defaultValue, Map<IndexValueWrapper, Value> values) {
        this.defaultValue = defaultValue;
        this.values = values;
    }

    /**
     * Creates a new ArrayValue where the value of not every index is known.
     *
     * @return the new array value
     */
    public static ArrayValue unknown() {
        return new ArrayValue(new UnknownValue(), new HashMap<>());
    }

    public static <T> ArrayValue fromNew(CtNewArray<T> ctNewArray) {
        if (!(ctNewArray.getType() instanceof CtArrayTypeReference<?> arrayType)) {
            throw new IllegalArgumentException("Unknown array type: " + ctNewArray.getType());
        }

        CtLiteral<?> defaultValue = SpoonUtil.getDefaultValue(arrayType.getArrayType());

        Map<IndexValueWrapper, Value> values = new HashMap<>();
        int i = 0;
        for (CtExpression<?> expression : ctNewArray.getElements()) {
            values.put(new IndexValueWrapper(VariableValue.fromInteger(i)), VariableValue.fromExpression(expression));
            i += 1;
        }

        return new ArrayValue(VariableValue.fromLiteral(defaultValue), values);
    }

    public void set(IndexValue index, Value value) {
        this.values.put(new IndexValueWrapper(index), value);
    }

    private Value defaultValue() {
        return this.defaultValue;
    }

    public void invalidate() {
        this.defaultValue = new UnknownValue();
        this.values.clear();
    }

    public Value get(IndexValue index, Scope scope) {
        // first try to get the value from the map
        Value potentialValue = this.values.get(new IndexValueWrapper(index));
        // if it is present, return it
        if (potentialValue != null) {
            return potentialValue;
        }

        // if it is not present, try to resolve the index based on the provided scope:
        return index.toExpression()
                    .map(scope::resolve)
                    // after it has been resolved, try to get the value again:
                    .flatMap(
                        resolved -> Optional.ofNullable(this.values.get(new IndexValueWrapper((IndexValue) resolved))))
                    // .map(value -> new VariableValue(value))
                    // if that is not possible either, return the default value:
                    .orElseGet(this::defaultValue);
    }

    @Override
    public Optional<CtExpression<?>> toExpression() {
        // if the array has no associated values, everything is the default value:
        if (this.values.isEmpty()) {
            // of course if the default value is unknown, empty will be returned
            return this.defaultValue().toExpression();
        }

        // otherwise check if all the values are the same as the default value:
        Value result = this.defaultValue();
        for (Value value : this.values.values()) {
            if (!value.equals(result)) {
                // not all are equal, so the expression is not known
                return Optional.empty();
            }
        }

        return result.toExpression();
    }


    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof ArrayValue that)) {
            return false;
        }

        return this.defaultValue().equals(that.defaultValue()) && this.values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.defaultValue, this.values);
    }
}
