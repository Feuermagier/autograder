package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.InCodeProblem;
import de.firemage.autograder.core.check.Check;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.nio.file.Path;

public class SpotbugsInCodeProblem extends InCodeProblem {

    public SpotbugsInCodeProblem(Check check, BugInstance bug) {
        super(check,
                mapLineAnnotation(bug.getPrimarySourceLineAnnotation()),
                bug.getMessageWithoutPrefix()
        );
    }

    private static CodePosition mapLineAnnotation(SourceLineAnnotation annotation) {
        return new CodePosition(
                Path.of(annotation.getRealSourcePath()),
                annotation.getStartLine(),
                annotation.getEndLine(),
                -1,
                -1
        );
    }
}
