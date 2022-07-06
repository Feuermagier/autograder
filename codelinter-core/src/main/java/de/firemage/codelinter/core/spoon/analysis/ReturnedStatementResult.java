package de.firemage.codelinter.core.spoon.analysis;

public final class ReturnedStatementResult implements StatementControlFlowResult {
    private final VariableState returnedValue;

    public ReturnedStatementResult(VariableState returnedValue) {
        this.returnedValue = returnedValue;
    }

    public VariableState getReturnedValue() {
        return returnedValue;
    }
}
