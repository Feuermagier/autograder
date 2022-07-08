package de.firemage.autograder.core.spoon.analysis;

public sealed interface StatementControlFlowResult
    permits ExceptionStatementResult, NonBreakingStatementResult, ReturnedStatementResult {
}
