package de.firemage.codelinter.core;

import de.firemage.codelinter.core.compiler.CompilationDiagnostic;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.compiler.CompilationResult;
import de.firemage.codelinter.core.compiler.Compiler;
import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.cpd.CPDLinter;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.pmd.PMDLinter;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonLinter;
import de.firemage.codelinter.core.spotbugs.SpotbugsLinter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class Linter implements AutoCloseable {
    private final UploadedFile file;

    private File jar = null;

    public Linter(UploadedFile file) {
        this.file = file;
    }

    public List<Problem> executeSpoonLints(JavaVersion javaVersion) throws CompilationException, IOException {
        if (this.jar == null) {
            throw new IllegalStateException("You have to call compile() before executing Spoon lints");
        }
        return new SpoonLinter().lint(this.file, javaVersion, this.jar);
    }

    public List<Problem> executePMDLints(Path ruleset) throws IOException {
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
            if (!this.jar.delete()) {
                log.warn("Could not delete jar file '" + this.jar.getAbsolutePath() + "'");
            }
        }
    }
}
