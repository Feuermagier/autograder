package de.firemage.codelinter.main.result;

public record CompilationErrorResult(String description) implements LintingResult {
}
