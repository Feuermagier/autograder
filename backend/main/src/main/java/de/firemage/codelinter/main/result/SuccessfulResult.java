package de.firemage.codelinter.main.result;

import java.util.Optional;

public class SuccessfulResult implements LintingResult {
    private final SpoonResult spoonResult;
    private final PMDResult pmdResult;


    public SuccessfulResult(SpoonResult spoonResult, PMDResult pmdResult) {
        this.spoonResult = spoonResult;
        this.pmdResult = pmdResult;
    }

    public Optional<SpoonResult> getSpoonResult() {
        return Optional.ofNullable(this.spoonResult);
    }

    public Optional<PMDResult> getPMDResult() {
        return Optional.ofNullable(this.pmdResult);
    }
}
