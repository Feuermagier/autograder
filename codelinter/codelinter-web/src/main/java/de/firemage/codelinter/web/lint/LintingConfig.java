package de.firemage.codelinter.web.lint;

import de.firemage.codelinter.core.compiler.JavaVersion;

public record LintingConfig(boolean enableSpoon, boolean enablePMD, boolean compile, boolean enableSpotbugs, boolean enableCPD, JavaVersion javaVersion) {
    public LintingConfig {
        if (enableSpotbugs && !compile) {
            throw new IllegalArgumentException("If you enable spotbugs you also have to compile the code");
        }

    }
}
