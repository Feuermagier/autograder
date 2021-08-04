package de.firemage.codelinter.linter;

import de.firemage.codelinter.linter.compiler.JavaVersion;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.pmd.PMDLinter;
import de.firemage.codelinter.linter.pmd.PMDRuleset;
import de.firemage.codelinter.linter.spoon.CompilationException;
import de.firemage.codelinter.linter.spoon.SpoonLinter;

import java.io.IOException;
import java.util.List;

public class Linter {
    private final UploadedFile file;

    public Linter(UploadedFile file) {
        this.file = file;
    }

    public List<Problem> executeSpoonLints(JavaVersion javaVersion) throws CompilationException {
        return new SpoonLinter().lint(this.file, javaVersion);
    }

    public List<Problem> executePMDLints(PMDRuleset ruleset) throws IOException {
        return new PMDLinter().lint(this.file, ruleset);
    }
}
