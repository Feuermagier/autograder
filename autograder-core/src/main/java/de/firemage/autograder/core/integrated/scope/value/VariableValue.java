package de.firemage.autograder.core.integrated.scope.value;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.Objects;
import java.util.Optional;

public final class VariableValue implements Value, IndexValue {
    private final CtExpression<?> value;

    private VariableValue(CtExpression<?> value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> IndexValue fromExpression(CtExpression<T> expression) {
        return new VariableValue(Objects.requireNonNull(expression));
    }

    public static <T> IndexValue fromLiteral(CtLiteral<T> literal) {
        return VariableValue.fromExpression(literal);
    }

    public static IndexValue fromInteger(int value) {
        return new VariableValue(SpoonUtil.makeLiteral(value));
    }

    @Override
    public Optional<CtExpression<?>> toExpression() {
        return Optional.of(this.value);
    }

    @Override
    public boolean isEqual(IndexValue other) {
        return this.equals(other);
    }

    @Override
    public int hashValue() {
        return this.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof Value that)) {
            return false;
        }

        CtExpression<?> left = this.toExpression().orElse(null);
        CtExpression<?> right = that.toExpression().orElse(null);

        if (left instanceof CtLiteral<?> leftLit && right instanceof CtLiteral<?> rightLit) {
            return SpoonUtil.areLiteralsEqual(leftLit, rightLit);
        }

        return this.toExpression().equals(that.toExpression());
    }

    @Override
    public int hashCode() {
        return this.toExpression().hashCode();
    }

    @Override
    public String toString() {
        return this.toExpression().map(Object::toString).orElse("null");
    }
}
