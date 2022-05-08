package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.CodePosition;
import de.firemage.codelinter.core.InCodeProblem;
import net.sourceforge.pmd.RuleViolation;
import java.nio.file.Path;

public class PMDInCodeProblem extends InCodeProblem {

    public PMDInCodeProblem(Check check, RuleViolation violation) {
        super(check,
                new CodePosition(Path.of(violation.getFilename()), violation.getBeginLine(), violation.getEndLine(), violation.getBeginColumn(), violation.getEndColumn()),
                check.getDescription());
    }
}
