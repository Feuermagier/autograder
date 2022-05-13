package de.firemage.codelinter.core.spoon.analysis;

public sealed interface StatementControlFlowResult
    permits ExceptionStatementResult, NonBreakingStatementResult, ReturnedStatementResult {
}
