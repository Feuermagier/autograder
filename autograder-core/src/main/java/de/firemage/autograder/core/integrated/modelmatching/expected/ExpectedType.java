package de.firemage.autograder.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.firemage.autograder.core.integrated.modelmatching.MatchUtil;
import lombok.Getter;
import spoon.reflect.declaration.CtType;

import java.util.List;

public class ExpectedType {
    @Getter
    @JsonProperty
    private List<String> names;

    @Getter
    @JsonProperty
    private List<ExpectedTypeOption> options;

    public double calculateMatchScore(CtType<?> type) {
        return MatchUtil.nameMatchScore(type.getSimpleName(), this.names)
            + this.options.stream()
            .mapToDouble(option -> option.calculateMatchScore(type))
            .max()
            .getAsDouble();
    }
}
