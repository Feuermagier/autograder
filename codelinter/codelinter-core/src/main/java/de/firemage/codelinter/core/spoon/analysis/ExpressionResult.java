package de.firemage.codelinter.core.spoon.analysis;

public class ExpressionResult {
    private final VariableState result;
    private final ThrownException thrownException;

    public ExpressionResult(VariableState result) {
        this.result = result;
        this.thrownException = null;
    }

    public ExpressionResult(ThrownException thrownException) {
        this.result = null;
        this.thrownException = thrownException;
    }

    public boolean isException() {
        return this.thrownException != null;
    }

    public boolean isResult() {
        return this.result != null;
    }

    public VariableState getResult() {
        if (this.result != null) {
            return this.result;
        } else {
            throw new IllegalStateException();
        }
    }

    public ThrownException getThrownException() {
        if (this.thrownException != null) {
            return this.thrownException;
        } else {
            throw new IllegalStateException();
        }
    }
}
