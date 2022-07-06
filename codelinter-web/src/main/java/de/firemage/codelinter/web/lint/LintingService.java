package de.firemage.codelinter.web.lint;

import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.web.result.LintingResult;

public interface LintingService {
    LintingResult lint(UploadedFile file, LintingConfig config);
}
