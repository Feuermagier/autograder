package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
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
