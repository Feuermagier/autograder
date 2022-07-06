package de.firemage.codelinter.core.spotbugs;

import de.firemage.codelinter.core.check.Check;
import de.firemage.codelinter.core.CodePosition;
import de.firemage.codelinter.core.InCodeProblem;
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
