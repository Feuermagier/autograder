package de.firemage.codelinter.web.lint;

import de.firemage.codelinter.core.Linter;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.compiler.CompilationDiagnostic;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.pmd.PMDRuleset;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.web.result.CPDResult;
import de.firemage.codelinter.web.result.CompilationErrorResult;
import de.firemage.codelinter.web.result.CompilationResult;
import de.firemage.codelinter.web.result.InternalErrorResult;
import de.firemage.codelinter.web.result.LintingResult;
import de.firemage.codelinter.web.result.PMDConfig;
import de.firemage.codelinter.web.result.PMDResult;
import de.firemage.codelinter.web.result.SpoonResult;
import de.firemage.codelinter.web.result.SpotbugsResult;
import de.firemage.codelinter.web.result.SuccessfulResult;
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
                    List<CompilationDiagnostic> diagnostics = linter.compile(config.javaVersion(), file.getFile().getParentFile());
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
                } catch (IOException e) {
                    return new InternalErrorResult(e.getMessage());
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

            CPDResult cpdResult = null;
            if (config.enableCPD()) {
                try {
                    log.debug("Executing CPD...");
                    List<Problem> problems = linter.executeCPDLints();
                    cpdResult = CPDResult.fromProblems(problems);
                    log.debug("CPD completed");
                } catch (IOException e) {
                    return new InternalErrorResult(e.getMessage());
                }
            }

            return new SuccessfulResult(spoonResult, pmdResult, compilationResult, spotbugsResult, cpdResult);
        } catch (IOException e) {
            return new InternalErrorResult(e.getMessage());
        }
    }
}
