package de.firemage.codelinter.core;

public enum ProblemPriority {
    /**
     * Problems that must be fixed or the student may lose many points.
     * Such problems indicate severe architectural issues, severe conflicts with OOP design principles
     * or usage of reflection.
     */
    SEVERE,

    /**
     * Problems that are as severe as 'SEVERE' problems, but there may be cases where it is fine to use a
     * specific cases and those cases are not reliably captured by the check.
     */
    POSSIBLE_SEVERE,

    /**
     * Problems that will lose you points, but don't indicate severe design problems.
     */
    FIX_RECOMMENDED,

    /**
     * Problems that will most likely not impact your grade directly, if there are not too many of them.
     */
    INFO
}
