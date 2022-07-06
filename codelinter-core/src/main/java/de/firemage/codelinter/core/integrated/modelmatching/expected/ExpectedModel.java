package de.firemage.codelinter.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

public class ExpectedModel {
    @Getter
    @JsonProperty
    private Map<String, ExpectedType> types;
}
