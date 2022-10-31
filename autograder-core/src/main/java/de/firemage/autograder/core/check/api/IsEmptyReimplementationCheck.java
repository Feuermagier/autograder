package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UseCollectionIsEmptyRule;

public class IsEmptyReimplementationCheck extends PMDCheck {
    public static final LocalizedMessage DESCRIPTION = new LocalizedMessage("is-empty-reimplemented-desc");

    public IsEmptyReimplementationCheck() {
        super(DESCRIPTION, new LocalizedMessage("is-empty-reimplemented-exp"), new UseCollectionIsEmptyRule(),
            ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED);
    }
}
