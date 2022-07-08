package de.firemage.autograder.core.spoon.analysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.reference.CtTypeReference;

public class VariableState {
    private final VariableType type;
    // We can prove that those values are possible
    private final ValueSet subset;
    // We can prove that the values not in this set are not possible
    private final ValueSet superset;

    public VariableState(VariableState firstBranch, VariableState secondBranch) {
        this(firstBranch.type, firstBranch.subset.intersect(secondBranch.subset),
            firstBranch.superset.combine(secondBranch.superset));

        if (firstBranch.getType() != secondBranch.getType()) {
            throw new IllegalArgumentException();
        }
    }

    public VariableState(VariableState origin) {
        this(origin.type, origin.subset.copy(), origin.superset.copy());
    }

    public VariableState(VariableType type, ValueSet subAndSuperset) {
        this(type, subAndSuperset, subAndSuperset);
    }

    public VariableState(VariableType type, ValueSet subset, ValueSet superset) {
        this.type = type;
        this.subset = subset;
        this.superset = superset;
    }

    public static VariableState defaultForType(CtTypeReference<?> type) {
        return switch(type.getQualifiedName()) {
            case "boolean" -> defaultForBoolean();
            case "char" -> throw new UnsupportedOperationException();
            case "byte" -> throw new UnsupportedOperationException();
            case "short" -> throw new UnsupportedOperationException();
            case "int" -> throw new UnsupportedOperationException();
            case "long" -> throw new UnsupportedOperationException();
            case "float" -> throw new UnsupportedOperationException();
            case "double" -> throw new UnsupportedOperationException();
            default -> defaultForReference();
        };
    }

    public static VariableState defaultForReference() {
        return new VariableState(VariableType.REFERENCE, new ObjectValueSet(false, false),
            new ObjectValueSet(true, true));
    }

    public static VariableState defaultForBoolean() {
        return new VariableState(VariableType.BOOLEAN, new BooleanValueSet(false, false),
            new BooleanValueSet(true, true));
    }

    public ValueSet getSubset() {
        return subset;
    }

    public ValueSet getSuperset() {
        return superset;
    }

    public VariableState handleUnaryOperator(UnaryOperatorKind operator) {
        return new VariableState(this.type, this.subset.handleUnaryOperator(operator),
            this.superset.handleUnaryOperator(operator));
    }

    public VariableState handleBinaryOperator(BinaryOperatorKind operator, VariableState other) {
        return new VariableState(this.type, this.subset.handleBinaryOperatorAsSubset(operator, other.subset),
            this.superset.handleBinaryOperatorAsSuperset(operator, other.superset));
    }

    public VariableType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "VariableState{" +
            "subset=" + subset +
            ", superset=" + superset +
            '}';
    }
}
