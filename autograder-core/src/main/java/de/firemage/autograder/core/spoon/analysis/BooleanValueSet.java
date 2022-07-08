package de.firemage.autograder.core.spoon.analysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.UnaryOperatorKind;

public final record BooleanValueSet(boolean containsTrue, boolean containsFalse) implements ValueSet {

    public BooleanValueSet invert() {
        return new BooleanValueSet(!this.containsTrue, !this.containsFalse);
    }

    @Override
    public BooleanValueSet intersect(ValueSet other) {
        if (other instanceof BooleanValueSet otherSet) {
            return new BooleanValueSet(this.containsTrue && otherSet.containsTrue,
                this.containsFalse && otherSet.containsFalse);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public BooleanValueSet combine(ValueSet other) {
        if (other instanceof BooleanValueSet otherSet) {
            return new BooleanValueSet(this.containsTrue || otherSet.containsTrue,
                this.containsFalse || otherSet.containsFalse);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public ValueSet copy() {
        return new BooleanValueSet(this.containsTrue, this.containsFalse);
    }

    @Override
    public BooleanValueSet handleUnaryOperator(UnaryOperatorKind operator) {
        return switch(operator) {
            case NOT -> this.invert();
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public ValueSet handleBinaryOperatorAsSubset(BinaryOperatorKind operator, ValueSet other) {
        if (other instanceof BooleanValueSet otherSet) {
            return switch(operator) {
                case OR -> new BooleanValueSet(this.containsTrue || otherSet.containsTrue,
                    this.containsFalse && otherSet.containsFalse);
                case AND -> new BooleanValueSet(this.containsTrue && otherSet.containsTrue,
                    this.containsFalse || otherSet.containsFalse);
                case EQ -> new BooleanValueSet((this.containsTrue && otherSet.containsTrue) || (this.containsFalse &&
                    otherSet.containsFalse),
                    (this.containsTrue && otherSet.containsFalse) ||
                        (this.containsFalse && (otherSet.containsFalse())));
                case NE -> new BooleanValueSet((this.containsTrue && otherSet.containsFalse) || (this.containsFalse &&
                    otherSet.containsTrue),
                    (this.containsTrue && otherSet.containsTrue) || (this.containsFalse && otherSet.containsFalse));
                default -> throw new IllegalStateException();
            };
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public ValueSet handleBinaryOperatorAsSuperset(BinaryOperatorKind operator, ValueSet other) {
        // We have all information available, so the implementations for the sub- and the superset are the same
        return handleBinaryOperatorAsSubset(operator, other);
    }
}
