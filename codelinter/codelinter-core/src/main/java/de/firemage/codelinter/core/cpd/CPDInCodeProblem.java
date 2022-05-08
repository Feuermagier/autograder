package de.firemage.codelinter.core.cpd;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.CodePosition;
import de.firemage.codelinter.core.MultiPositionProblem;
import de.firemage.codelinter.core.PathUtil;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import java.nio.file.Path;
import java.util.List;

public class CPDInCodeProblem extends MultiPositionProblem {

    private final Match match;
    private final Path root;

    public CPDInCodeProblem(Check check, Match match, Path root) {
        super(check,
                List.of(
                        markToPosition(match.getFirstMark()),
                        markToPosition(match.getSecondMark())
                ),
                formatMatch(match, root));
        this.match = match;
        this.root = root;
    }

    private static String formatMatch(Match match, Path root) {
        return "Duplicated code (" + match.getLineCount() + " lines): "
                + formatLocation(match, root);
    }

    private static String formatLocation(Match match, Path root) {
        return PathUtil.getSanitizedPath(match.getFirstMark().getFilename(), root) + ":"
                + match.getFirstMark().getBeginLine() + "-" + match.getFirstMark().getEndLine()
                + " and "
                + PathUtil.getSanitizedPath(match.getSecondMark().getFilename(), root) + ":"
                + match.getSecondMark().getBeginLine() + "-" + match.getSecondMark().getEndLine();
    }

    private static CodePosition markToPosition(Mark mark) {
        return new CodePosition(Path.of(mark.getFilename()), mark.getBeginLine(), mark.getEndLine(), mark.getBeginColumn(), mark.getEndColumn());
    }
}
