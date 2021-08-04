package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.compiler.JavaVersion;

public record LintingConfig(boolean enableSpoon, boolean enablePMD, boolean compile, JavaVersion javaVersion) {
}
