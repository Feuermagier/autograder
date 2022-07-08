package de.firemage.autograder.core.integrated.modelmatching.expected;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.Map;

@JsonTypeName("class")
public final class ExpectedClass extends ExpectedTypeOption {
    @Getter
    @JsonProperty
    private Map<String, ExpectedMember> members;

    @Getter
    @JsonProperty
    private Map<String, ExpectedMethod> methods;

    @JsonIgnore
    @Getter
    private List<Candidate<CtClass<?>>> candidates;

    @Override
    public double calculateMatchScore(CtType<?> type) {
        double score = 0.0;

        if (type instanceof CtClass<?> clazz) {
            for (CtMethod<?> method : clazz.getMethods()) {
                score += 0;
                for (Map.Entry<String, ExpectedMethod> expectedMethods : this.methods.entrySet()) {

                }
            }
        }

        return score;
    }
}
