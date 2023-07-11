package de.firemage.autograder.core.cpd;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.SourcePath;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.nio.file.Path;
import java.util.Map;

class CPDInCodeProblem extends ProblemImpl {
    private final Match match;
    private final SourceInfo sourceInfo;

    public CPDInCodeProblem(Check check, Match match, SourceInfo sourceInfo) {
        super(
            check,
            markToPosition(sourceInfo, match.getFirstMark()),
            formatMatch(match, sourceInfo),
            ProblemType.DUPLICATE_CODE
        );
        this.match = match;
        this.sourceInfo = sourceInfo;
    }

    private static LocalizedMessage formatMatch(Match match, SourceInfo root) {
        SourcePath firstPath = root.getCompilationUnit(Path.of(match.getFirstMark().getFilename())).path();
        SourcePath secondPath = root.getCompilationUnit(Path.of(match.getSecondMark().getFilename())).path();

        return new LocalizedMessage("duplicate-code", Map.of(
            "lines", String.valueOf(match.getLineCount()),
            "first-path", firstPath,
            "first-start", String.valueOf(match.getFirstMark().getBeginLine()),
            "first-end", String.valueOf(match.getFirstMark().getEndLine()),
            "second-path", secondPath,
            "second-start", String.valueOf(match.getSecondMark().getBeginLine()),
            "second-end", String.valueOf(match.getSecondMark().getEndLine())
        ));
    }

    private static CodePosition markToPosition(SourceInfo sourceInfo, Mark mark) {
        // TODO mark.getFilename() is most likely not correct
        return new CodePosition(sourceInfo, SourcePath.of(mark.getFilename()), mark.getBeginLine(), mark.getEndLine(),
            mark.getBeginColumn(), mark.getEndColumn());
    }
}
