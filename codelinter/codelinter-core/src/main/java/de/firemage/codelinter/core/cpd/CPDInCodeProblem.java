package de.firemage.codelinter.core.cpd;

import de.firemage.codelinter.core.InCodeProblem;
import de.firemage.codelinter.core.PathUtil;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import net.sourceforge.pmd.cpd.Match;
import java.io.File;

public class CPDInCodeProblem extends InCodeProblem {

    private final Match match;
    private final File root;

    public CPDInCodeProblem(Match match, File root) {
        super(match.getFirstMark().getFilename(),
                match.getFirstMark().getBeginLine(),
                match.getFirstMark().getBeginColumn(), formatMatch(match, root),
                ProblemCategory.BAD_STYLE,
                """
                        Duplicated code is hard to maintain and should always be avoided.
                        Try to create a helper method, a helper class or an utility class""",
                ProblemPriority.FIX_RECOMMENDED);
        this.match = match;
        this.root = root;
    }

    private static String formatMatch(Match match, File root) {
        return "Duplicated code (" + match.getLineCount() + " lines): "
                + formatLocation(match, root);
    }

    private static String formatLocation(Match match, File root) {
        return PathUtil.getSanitizedPath(match.getFirstMark().getFilename(), root) + ":"
                + match.getFirstMark().getBeginLine() + "-" + match.getFirstMark().getEndLine()
                + " and "
                + PathUtil.getSanitizedPath(match.getSecondMark().getFilename(), root) + ":"
                + match.getSecondMark().getBeginLine() + "-" + match.getSecondMark().getEndLine();
    }

    @Override
    public String getDisplayLocation() {
        return formatLocation(this.match, root);
    }
}
