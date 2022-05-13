package de.firemage.codelinter.core.spoon.analysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.UnaryOperatorKind;

public final record ObjectValueSet(boolean containsNull, boolean containsInstance) implements ValueSet {

    @Override
    public ObjectValueSet intersect(ValueSet other) {
        if (other instanceof ObjectValueSet otherSet) {
            return new ObjectValueSet(this.containsNull && otherSet.containsNull,
                this.containsInstance && otherSet.containsInstance);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public ObjectValueSet combine(ValueSet other) {
        if (other instanceof ObjectValueSet otherSet) {
            return new ObjectValueSet(this.containsNull || otherSet.containsNull,
                this.containsInstance || otherSet.containsInstance);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public ObjectValueSet copy() {
        return new ObjectValueSet(this.containsNull, this.containsInstance);
    }

    @Override
    public ValueSet handleUnaryOperator(UnaryOperatorKind operator) {
        throw new IllegalStateException();
    }

    @Override
    public ValueSet handleBinaryOperatorAsSubset(BinaryOperatorKind operator, ValueSet other) {
        if (other instanceof ObjectValueSet otherSet) {
            return switch(operator) {
                case EQ -> new BooleanValueSet(this.containsNull && otherSet.containsNull,
                    (this.containsNull && otherSet.containsInstance) ||
                        (this.containsInstance && otherSet.containsNull));
                case NE -> new BooleanValueSet((this.containsNull && otherSet.containsInstance) ||
                    (this.containsInstance && otherSet.containsNull), this.containsNull && otherSet.containsNull);
                default -> throw new IllegalStateException();
            };
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public ValueSet handleBinaryOperatorAsSuperset(BinaryOperatorKind operator, ValueSet other) {
        if (other instanceof ObjectValueSet otherSet) {
            return switch(operator) {
                case EQ -> new BooleanValueSet(
                    (this.containsNull && otherSet.containsNull) ||
                        (this.containsInstance && otherSet.containsInstance),
                    this.containsInstance || otherSet.containsInstance);
                case NE -> new BooleanValueSet(
                    this.containsInstance || otherSet.containsInstance,
                    (this.containsNull && otherSet.containsNull) ||
                        (this.containsInstance && otherSet.containsInstance));
                default -> throw new IllegalStateException();
            };
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "ObjectValueSet{" +
            "containsNull=" + containsNull +
            ", containsInstance=" + containsInstance +
            '}';
    }
}
