package de.firemage.autograder.core.check;

import de.firemage.autograder.core.ProblemType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutableCheck {
    ProblemType[] reportedProblems();
}
