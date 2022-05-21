package de.firemage.codelinter.core;

import de.firemage.codelinter.core.check.Check;
import de.firemage.codelinter.core.check.CopyPasteCheck;
import de.firemage.codelinter.core.compiler.CompilationResult;
import de.firemage.codelinter.core.compiler.Compiler;
import de.firemage.codelinter.core.cpd.CPDLinter;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.integrated.IntegratedAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.pmd.PMDCheck;
import de.firemage.codelinter.core.pmd.PMDLinter;
import de.firemage.codelinter.core.spotbugs.SpotbugsCheck;
import de.firemage.codelinter.core.spotbugs.SpotbugsLinter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class Linter {

    public List<Problem> checkFile(UploadedFile file, Path tmpLocation, Path tests, List<Check> checks, Consumer<String> statusConsumer)
            throws LinterException, InterruptedException, IOException {
        statusConsumer.accept("Compiling");
        CompilationResult result = Compiler.compileToJar(file, tmpLocation, file.getVersion());

        List<PMDCheck> pmdChecks = new ArrayList<>();
        List<SpotbugsCheck> spotbugsChecks = new ArrayList<>();
        List<CopyPasteCheck> cpdChecks = new ArrayList<>();
        List<IntegratedCheck> integratedChecks = new ArrayList<>();

        for (Check check : checks) {
            if (check instanceof PMDCheck pmdCheck) {
                pmdChecks.add(pmdCheck);
            } else if (check instanceof CopyPasteCheck cpdCheck) {
                cpdChecks.add(cpdCheck);
            } else if (check instanceof SpotbugsCheck spotbugsCheck) {
                spotbugsChecks.add(spotbugsCheck);
            } else if (check instanceof IntegratedCheck integratedCheck) {
                integratedChecks.add(integratedCheck);
            } else {
                throw new IllegalStateException();
            }
        }

        List<Problem> problems = new ArrayList<>();

        if (!pmdChecks.isEmpty()) {
            statusConsumer.accept("Running PMD");
            problems.addAll(new PMDLinter().lint(file, pmdChecks));
        }

        if (!cpdChecks.isEmpty()) {
            statusConsumer.accept("Running CPD");
            problems.addAll(new CPDLinter().lint(file, cpdChecks));
        }

        if (!spotbugsChecks.isEmpty()) {
            statusConsumer.accept("Running SpotBugs");
            problems.addAll(new SpotbugsLinter().lint(result.jar(), spotbugsChecks));
        }

        if (!integratedChecks.isEmpty()) {
            statusConsumer.accept("Building the code model");
            try (IntegratedAnalysis analysis = new IntegratedAnalysis(file, result.jar(), statusConsumer)) {
                analysis.runDynamicAnalysis(tests, statusConsumer);
                problems.addAll(analysis.lint(integratedChecks));
            }
        }


        result.jar().toFile().delete();
        return problems;
    }
}
