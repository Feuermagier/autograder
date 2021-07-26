package de.firemage.codelinter.main.lint;

import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.main.result.LintingResult;

public interface LintingService {
    LintingResult lint(UploadedFile file, LintingConfig config);
}
