package de.firemage.codelinter.core.spoon;

import de.firemage.codelinter.core.GlobalProblem;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;

public class SpoonGlobalProblem extends GlobalProblem {
    public SpoonGlobalProblem(String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        super(description, category, explanation, priority);
    }
}
