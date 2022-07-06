package de.firemage.codelinter.web.result;

public record InternalErrorResult(String description) implements LintingResult {
}
