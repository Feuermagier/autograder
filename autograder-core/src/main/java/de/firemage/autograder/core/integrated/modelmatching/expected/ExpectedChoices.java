package de.firemage.autograder.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import spoon.reflect.declaration.CtType;

import java.util.List;

@JsonTypeName("choices")
public final class ExpectedChoices extends ExpectedTypeOption {
    @Getter
    @JsonProperty
    private List<List<String>> values;

    @Override
    public double calculateMatchScore(CtType<?> type) {
        return 0;
    }
}
