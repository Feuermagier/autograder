package de.firemage.autograder.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public class ExpectedMethod {
    @Getter
    @JsonProperty
    private List<String> names;

    @Getter
    @JsonProperty
    private String ret;

    @Getter
    @JsonProperty
    private List<String> params;
}
