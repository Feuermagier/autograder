package de.firemage.codelinter.linter;

import de.firemage.codelinter.linter.compiler.CompilationDiagnostic;
import de.firemage.codelinter.linter.compiler.CompilationFailureException;
import de.firemage.codelinter.linter.compiler.CompilationResult;
import de.firemage.codelinter.linter.compiler.Compiler;
import de.firemage.codelinter.linter.compiler.JavaVersion;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.pmd.PMDLinter;
import de.firemage.codelinter.linter.pmd.PMDRuleset;
import de.firemage.codelinter.linter.spoon.CompilationException;
import de.firemage.codelinter.linter.spoon.SpoonLinter;
import de.firemage.codelinter.linter.spotbugs.SpotbugsLinter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Linter implements AutoCloseable {
    private final UploadedFile file;

    private File jar = null;

    public Linter(UploadedFile file) {
        this.file = file;
    }

    public List<Problem> executeSpoonLints(JavaVersion javaVersion) throws CompilationException {
        return new SpoonLinter().lint(this.file, javaVersion);
    }

    public List<Problem> executePMDLints(PMDRuleset ruleset) throws IOException {
        return new PMDLinter().lint(this.file, ruleset);
    }

    public List<CompilationDiagnostic> compile(JavaVersion javaVersion) throws IOException, CompilationFailureException {
        CompilationResult result = Compiler.compileToJar(this.file, javaVersion);
        this.jar = result.jar();
        return result.diagnostics();
    }

    public List<Problem> executeSpotbugsLints() throws IOException, InterruptedException {
        if (this.jar == null) {
            throw new IllegalStateException("You have to call compile() before executing Spotbugs");
        }

        return new SpotbugsLinter().lint(this.jar);
    }

    @Override
    public void close() {
        if (this.jar != null) {
            this.jar.delete();
        }
    }
}
