package de.firemage.codelinter.core.spoon.analysis;

public class MethodAnalysisResult {
    private boolean canReturnNull = false;

    /* package */ void markAsNullable() {
        this.canReturnNull = true;
    }

    public boolean canReturnNull() {
        return canReturnNull;
    }

    @Override
    public String toString() {
        return "MethodAnalysisResult{" +
                "canReturnNull=" + canReturnNull +
                '}';
    }
}
