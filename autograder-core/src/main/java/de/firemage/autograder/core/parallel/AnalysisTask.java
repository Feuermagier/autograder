package de.firemage.autograder.core.parallel;

@FunctionalInterface
public interface AnalysisTask {
    void run(AnalysisScheduler scheduler, ProblemReporter reporter) throws Exception;
}
