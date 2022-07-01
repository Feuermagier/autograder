package de.firemage.codelinter.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import spoon.reflect.declaration.CtType;

public final class ExpectedPrimitive extends ExpectedTypeOption {
    @Getter
    @JsonValue
    private String type;

    @JsonCreator
    public ExpectedPrimitive(String type) {
        this.type = type;
    }

    @Override
    public double calculateMatchScore(CtType<?> type) {
        return 0;
    }
}
