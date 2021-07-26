package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.Linter;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.pmd.PMDRuleset;
import de.firemage.codelinter.linter.spoon.CompilationException;
import de.firemage.codelinter.linter.spoon.Problem;
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

        SpoonResult spoonResult = null;
        if (config.enableSpoon()) {
            List<Problem> problems = null;
            try {
                problems = linter.executeSpoonLints(config.javaLevel());
            } catch (CompilationException e) {
                return new CompilationErrorResult(e.getMessage());
            }
            spoonResult = SpoonResult.fromProblems(problems);
        }

        PMDResult pmdResult = null;
        if (config.enablePMD()) {
            String result = null;
            try {
                result = linter.executePMDLints(this.pmdRuleset);
            } catch (IOException e) {
                return new InternalErrorResult(e.getMessage());
            }
            pmdResult = new PMDResult(result);
        }

        return new SuccessfulResult(spoonResult, pmdResult);
    }
}
