package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.file.SourceInfo;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;

import java.nio.file.Path;

class SpotbugsInCodeProblem extends ProblemImpl {

    SpotbugsInCodeProblem(SpotbugsCheck check, BugInstance bug, SourceInfo sourceInfo) {
        super(check,
            mapLineAnnotation(bug.getPrimarySourceLineAnnotation(), sourceInfo),
            check.getExplanation(),
            check.getProblemType()
        );
    }

    private static CodePosition mapLineAnnotation(SourceLineAnnotation annotation, SourceInfo sourceInfo) {
        return new CodePosition(
            sourceInfo,
            sourceInfo.getCompilationUnit(Path.of(annotation.getRealSourcePath())).path(),
            annotation.getStartLine(),
            annotation.getEndLine(),
            -1,
            -1
        );
    }
}
