package de.firemage.codelinter.main.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

public record SuccessfulResult(@JsonIgnore SpoonResult spoonResult,
                               @JsonIgnore PMDResult pmdResult) implements LintingResult {

    @JsonInclude(value = JsonInclude.Include.NON_ABSENT)
    public Optional<SpoonResult> getSpoon() {
        return Optional.ofNullable(this.spoonResult);
    }

    @JsonInclude(value = JsonInclude.Include.NON_ABSENT)
    public Optional<PMDResult> getPMD() {
        return Optional.ofNullable(this.pmdResult);
    }
}
