package de.firemage.autograder.core.pmd;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.LocalizedMessage;
import net.sourceforge.pmd.RuleViolation;

import java.nio.file.Path;

public class PMDInCodeProblem extends ProblemImpl {

    public PMDInCodeProblem(PMDCheck check, RuleViolation violation, Path root) {
        super(check,
            new CodePosition(
                relativize(root, Path.of(violation.getFilename())),
                violation.getBeginLine(),
                violation.getBeginLine(),
                violation.getBeginColumn(),
                violation.getBeginColumn()),
            check.getExplanation() != null ? check.getExplanation() : new LocalizedMessage(violation.getDescription()),
            check.getProblemType());
    }

    private static Path relativize(Path root, Path file) {
        try {
            return root.normalize().toAbsolutePath().relativize(file.normalize().toAbsolutePath());
        } catch (IllegalArgumentException e) {
            // this happens if the file is not relative to the root
            return file;
        }
    }

    @Override
    public String toString() {
        return "PMDInCodeProblem[check=%s, position=%s, explanation=%s, problemType=%s]".formatted(
            this.getCheck(),
            this.getPosition(),
            this.getExplanation(),
            this.getProblemType()
        );
    }
}
