package de.firemage.codelinter.core;

import de.firemage.codelinter.core.check.CopyPasteCheck;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.compiler.CompilationResult;
import de.firemage.codelinter.core.compiler.Compiler;
import de.firemage.codelinter.core.cpd.CPDLinter;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.integrated.IntegratedAnalysis;
import de.firemage.codelinter.core.pmd.PMDCheck;
import de.firemage.codelinter.core.pmd.PMDLinter;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonCheck;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Linter {

    public List<Problem> checkFile(UploadedFile file, Path tmpLocation, List<Check> checks)
        throws LinterException, InterruptedException, IOException {
        CompilationResult result = Compiler.compileToJar(file, tmpLocation, file.getVersion());

        List<PMDCheck> pmdChecks = new ArrayList<>();
        List<CopyPasteCheck> cpdChecks = new ArrayList<>();
        List<SpoonCheck> spoonChecks = new ArrayList<>();

        for (Check check : checks) {
            if (check instanceof PMDCheck pmdCheck) {
                pmdChecks.add(pmdCheck);
            } else if (check instanceof CopyPasteCheck cpdCheck) {
                cpdChecks.add(cpdCheck);
            } else if (check instanceof SpoonCheck spoonCheck) {
                spoonChecks.add(spoonCheck);
            } else {
                throw new IllegalStateException();
            }
        }

        List<Problem> problems = new ArrayList<>();

        if (!pmdChecks.isEmpty()) {
            problems.addAll(new PMDLinter().lint(file, pmdChecks));
        }

        if (!cpdChecks.isEmpty()) {
            problems.addAll(new CPDLinter().lint(file, cpdChecks));
        }

        if (!spoonChecks.isEmpty()) {
            try (IntegratedAnalysis analysis = new IntegratedAnalysis(file, result.jar(), tmpLocation)) {
                analysis.runDynamicAnalysis();
                analysis.getDynamicAnalysis().findEventsForMethod(analysis.getStaticAnalysis().findMethodBySignature(
                        analysis.getStaticAnalysis().findClassByName("edu.kit.informatik.Simulation"),
                        "createTrainSet(java.lang.String,java.lang.String,int,boolean,boolean)"))
                    .forEach(e -> System.out.println(e.format()));
                problems.addAll(analysis.lint(spoonChecks));
            }
        }


        result.jar().toFile().delete();
        return problems;
    }
}
