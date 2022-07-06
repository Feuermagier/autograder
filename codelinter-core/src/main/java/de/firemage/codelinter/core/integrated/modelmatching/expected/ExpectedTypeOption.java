package de.firemage.codelinter.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import spoon.reflect.declaration.CtType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExpectedPrimitive.class, name = "primitive"),
    @JsonSubTypes.Type(value = ExpectedClass.class, name = "class"),
    @JsonSubTypes.Type(value = ExpectedChoices.class, name = "choices")
})
public sealed abstract class ExpectedTypeOption permits ExpectedChoices, ExpectedClass, ExpectedPrimitive {
    public abstract double calculateMatchScore(CtType<?> type);
}
