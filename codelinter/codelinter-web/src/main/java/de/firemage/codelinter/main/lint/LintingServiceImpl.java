package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.Linter;
import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.compiler.CompilationDiagnostic;
import de.firemage.codelinter.linter.compiler.CompilationFailureException;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.pmd.PMDRuleset;
import de.firemage.codelinter.linter.spoon.CompilationException;
import de.firemage.codelinter.main.result.CompilationErrorResult;
import de.firemage.codelinter.main.result.CompilationResult;
import de.firemage.codelinter.main.result.InternalErrorResult;
import de.firemage.codelinter.main.result.LintingResult;
import de.firemage.codelinter.main.result.PMDConfig;
import de.firemage.codelinter.main.result.PMDResult;
import de.firemage.codelinter.main.result.SpoonResult;
import de.firemage.codelinter.main.result.SpotbugsResult;
import de.firemage.codelinter.main.result.SuccessfulResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class LintingServiceImpl implements LintingService {
    private final PMDRuleset pmdRuleset;

    @Autowired
    public LintingServiceImpl(PMDConfig pmdConfig) {
        this.pmdRuleset = pmdConfig.getRuleset();
    }

    @Override
    public LintingResult lint(UploadedFile file, LintingConfig config) {
        try (Linter linter = new Linter(file)) {
            CompilationResult compilationResult = null;
            if (config.compile()) {
                try {
                    log.debug("Starting compilation...");
                    List<CompilationDiagnostic> diagnostics = linter.compile(config.javaVersion());
                    compilationResult = CompilationResult.fromDiagnostics(diagnostics);
                    log.debug("Finished compilation");
                } catch (IOException | CompilationFailureException e) {
                    log.debug("Compilation failed", e);
                    return new CompilationErrorResult(e.getMessage());
                }
            }

            SpotbugsResult spotbugsResult = null;
            if (config.enableSpotbugs()) {
                try {
                    log.debug("Executing SpotBugs..");
                    List<Problem> problems = linter.executeSpotbugsLints();
                    spotbugsResult = SpotbugsResult.fromProblems(problems);
                    log.debug("SpotBugs analysis completed");
                } catch (IOException | InterruptedException e) {
                    return new InternalErrorResult(e.getMessage());
                }
            }

            SpoonResult spoonResult = null;
            if (config.enableSpoon()) {
                try {
                    log.debug("Executing Spoon lints...");
                    List<Problem> problems = linter.executeSpoonLints(config.javaVersion());
                    spoonResult = SpoonResult.fromProblems(problems);
                    log.debug("Spoon lints completed");
                } catch (CompilationException e) {
                    return new CompilationErrorResult(e.getMessage());
                }
            }

            PMDResult pmdResult = null;
            if (config.enablePMD()) {
                try {
                    log.debug("Executing PMD...");
                    List<Problem> problems = linter.executePMDLints(this.pmdRuleset);
                    pmdResult = PMDResult.fromProblems(problems);
                    log.debug("PMD analysis completed");
                } catch (IOException e) {
                    return new InternalErrorResult(e.getMessage());
                }
            }

            return new SuccessfulResult(spoonResult, pmdResult, compilationResult, spotbugsResult);
        }
    }
}
