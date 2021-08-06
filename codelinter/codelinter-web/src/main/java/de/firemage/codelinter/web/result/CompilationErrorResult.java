package de.firemage.codelinter.web.result;

public record CompilationErrorResult(String description) implements LintingResult {
}
