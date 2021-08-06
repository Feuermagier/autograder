package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.compiler.JavaVersion;

public record LintingConfig(boolean enableSpoon, boolean enablePMD, boolean compile, boolean enableSpotbugs, JavaVersion javaVersion) {
    public LintingConfig(boolean enableSpoon, boolean enablePMD, boolean compile, boolean enableSpotbugs, JavaVersion javaVersion) {
        if (enableSpotbugs && !compile) {
            throw new IllegalArgumentException("If you enable spotbugs you also have to compile the code");
        }

        this.enableSpoon = enableSpoon;
        this.enablePMD = enablePMD;
        this.compile = compile;
        this.enableSpotbugs = enableSpotbugs;
        this.javaVersion = javaVersion;
    }
}
