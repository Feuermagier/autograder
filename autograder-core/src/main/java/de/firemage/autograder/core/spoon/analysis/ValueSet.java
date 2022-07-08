package de.firemage.autograder.core.spoon.analysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.UnaryOperatorKind;

public sealed interface ValueSet permits BooleanValueSet, ObjectValueSet {
    ValueSet intersect(ValueSet other);

    ValueSet combine(ValueSet other);

    ValueSet copy();

    ValueSet handleUnaryOperator(UnaryOperatorKind operator);

    ValueSet handleBinaryOperatorAsSubset(BinaryOperatorKind operator, ValueSet other);
    ValueSet handleBinaryOperatorAsSuperset(BinaryOperatorKind operator, ValueSet other);
}
