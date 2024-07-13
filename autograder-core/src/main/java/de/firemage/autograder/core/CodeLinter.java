package de.firemage.autograder.core;

import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.api.AbstractTempLocation;
import de.firemage.autograder.core.file.UploadedFile;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface CodeLinter<T extends Check> {
    Class<? super T> supportedCheckType();

    /**
     * Lints the given submission using the given checks.
     *
     * @param submission the submission to lint
     * @param tempLocation in this location, temporary files can be stored by the linter
     * @param classLoader some class loader
     * @param checks the checks to use, they are guaranteed to be supported by this linter
     * @param statusConsumer a consumer that can be used to report the progress of the linting
     * @return a list of problems found in the submission
     * @throws IOException if an I/O error occurs
     */
    List<ProblemImpl> lint(
        UploadedFile submission,
        AbstractTempLocation tempLocation,
        ClassLoader classLoader,
        List<T> checks,
        Consumer<Translatable> statusConsumer
    ) throws IOException;
}
