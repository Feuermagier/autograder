package de.firemage.codelinter.core.spoon.analysis;

import spoon.reflect.reference.CtTypeReference;

public class MethodAnalysisResult {
    private VariableState returnState = null;

    public MethodAnalysisResult(CtTypeReference<?> type) {
        //this.returnValues = VariableState.defaultForType(type);
    }

    /* package */ void addReturnValue(VariableState state) {
        if (this.returnState != null) {
            this.returnState = new VariableState(returnState, state);
        } else {
            this.returnState = state;
        }
    }

    public VariableState getReturnState() {
        return this.returnState;
    }

    @Override
    public String toString() {
        return this.returnState.toString();
    }
}
