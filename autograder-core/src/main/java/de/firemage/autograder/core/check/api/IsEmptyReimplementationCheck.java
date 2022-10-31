package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UseCollectionIsEmptyRule;

public class IsEmptyReimplementationCheck extends PMDCheck {
    public static final String DESCRIPTION = "Use isEmpty() instead of size() == 0 or similar code";

    public IsEmptyReimplementationCheck() {
        super(DESCRIPTION, "Use isEmpty()", new UseCollectionIsEmptyRule(),
            ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED);
    }
}
