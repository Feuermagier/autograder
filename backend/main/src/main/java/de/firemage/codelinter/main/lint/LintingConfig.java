package de.firemage.codelinter.main.lint;

public record LintingConfig(boolean enableSpoon, boolean enablePMD, boolean compile, int javaLevel) {
}
