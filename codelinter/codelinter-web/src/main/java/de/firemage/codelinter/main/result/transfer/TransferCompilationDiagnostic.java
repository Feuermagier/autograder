package de.firemage.codelinter.main.result.transfer;

import de.firemage.codelinter.linter.compiler.CompilationDiagnostic;

public class TransferCompilationDiagnostic {
    private final CompilationDiagnostic diagnostic;

    public TransferCompilationDiagnostic(CompilationDiagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getMessage() {
        return this.diagnostic.message();
    }

    public int getLine() {
        return this.diagnostic.line();
    }

    public int getColumn() {
        return this.diagnostic.column();
    }

    public String getClassName() {
        return this.diagnostic.path();
    }
}
