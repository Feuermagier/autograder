package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.Linter;
import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.pmd.PMDRuleset;
import de.firemage.codelinter.linter.spoon.CompilationException;
import de.firemage.codelinter.main.result.CompilationErrorResult;
import de.firemage.codelinter.main.result.InternalErrorResult;
import de.firemage.codelinter.main.result.LintingResult;
import de.firemage.codelinter.main.result.PMDConfig;
import de.firemage.codelinter.main.result.PMDResult;
import de.firemage.codelinter.main.result.SpoonResult;
import de.firemage.codelinter.main.result.SuccessfulResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class LintingServiceImpl implements LintingService {
    private final PMDRuleset pmdRuleset;

    @Autowired
    public LintingServiceImpl(PMDConfig pmdConfig) {
        this.pmdRuleset = pmdConfig.getRuleset();
    }

    @Override
    public LintingResult lint(UploadedFile file, LintingConfig config) {
        Linter linter = new Linter(file);

        if (config.compile()) {
            try {
                file.compile();
            } catch (IOException e) {
                return new CompilationErrorResult(e.getMessage());
            }

            String out = file.getCompilerErrorOutput();
            if (out != null && !out.isBlank()) {
                return new CompilationErrorResult(out);
            }
        }

        SpoonResult spoonResult = null;
        if (config.enableSpoon()) {
            try {
                List<Problem> problems = linter.executeSpoonLints(config.javaLevel());
                spoonResult = SpoonResult.fromProblems(problems);
            } catch (CompilationException e) {
                return new CompilationErrorResult(e.getMessage());
            }
        }

        PMDResult pmdResult = null;
        if (config.enablePMD()) {
            try {
                List<Problem> problems = linter.executePMDLints(this.pmdRuleset);
                pmdResult = PMDResult.fromProblems(problems);
            } catch (IOException e) {
                return new InternalErrorResult(e.getMessage());
            }
        }

        return new SuccessfulResult(spoonResult, pmdResult);
    }
}
