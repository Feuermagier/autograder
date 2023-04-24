package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UseCollectionIsEmptyRule;

@ExecutableCheck(reportedProblems = {ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED})
public class IsEmptyReimplementationCheck extends PMDCheck {
    public IsEmptyReimplementationCheck() {
        super(new LocalizedMessage("is-empty-reimplemented-exp"), new UseCollectionIsEmptyRule(),
            ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED);
    }
}
