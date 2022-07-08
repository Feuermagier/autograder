package de.firemage.autograder.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class ExpectedMember {
    @Getter
    @JsonProperty
    private String type;

    @Getter
    @JsonProperty
    private List<String> names;
}
