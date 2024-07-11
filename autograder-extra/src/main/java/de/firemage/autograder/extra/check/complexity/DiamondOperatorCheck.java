package de.firemage.autograder.extra.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.extra.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UseDiamondOperatorRule;

import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.UNUSED_DIAMOND_OPERATOR})
public class DiamondOperatorCheck extends PMDCheck {
    public DiamondOperatorCheck() {
        super(
            new LocalizedMessage("use-diamond-operator"),
            new UseDiamondOperatorRule(),
            ProblemType.UNUSED_DIAMOND_OPERATOR
        );
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(3);
    }
}
