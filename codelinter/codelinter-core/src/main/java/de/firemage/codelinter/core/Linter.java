package de.firemage.codelinter.core;

import de.firemage.codelinter.core.compiler.CompilationDiagnostic;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.compiler.CompilationResult;
import de.firemage.codelinter.core.compiler.Compiler;
import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.cpd.CPDLinter;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.pmd.PMDLinter;
import de.firemage.codelinter.core.pmd.PMDRuleset;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonLinter;
import de.firemage.codelinter.core.spotbugs.SpotbugsLinter;

import java.io.File;
import java.io.IOException;
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

    public List<CompilationDiagnostic> compile(JavaVersion javaVersion, File tmpLocation) throws IOException, CompilationFailureException {
        CompilationResult result = Compiler.compileToJar(this.file, tmpLocation, javaVersion);
        this.jar = result.jar();
        return result.diagnostics();
    }

    public List<Problem> executeSpotbugsLints() throws IOException, InterruptedException {
        if (this.jar == null) {
            throw new IllegalStateException("You have to call compile() before executing Spotbugs");
        }

        return new SpotbugsLinter().lint(this.jar);
    }

    public List<Problem> executeCPDLints() throws IOException {
        return new CPDLinter().lint(this.file);
    }

    @Override
    public void close() {
        if (this.jar != null) {
            this.jar.delete();
        }
    }
}
