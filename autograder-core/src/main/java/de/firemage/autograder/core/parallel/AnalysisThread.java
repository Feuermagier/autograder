package de.firemage.autograder.core.parallel;

import de.firemage.autograder.api.Problem;
import de.firemage.autograder.core.ProblemImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnalysisThread {
    private final AnalysisScheduler scheduler;
    private final Thread thread;
    private final List<ProblemImpl> reportedProblems;
    private Exception thrownException;

    public AnalysisThread(AnalysisScheduler scheduler, int threadIndex) {
        this.scheduler = scheduler;
        this.reportedProblems = new ArrayList<>();
        this.thread = new Thread(this::run, "Autograder-Analysis-Thread-" + threadIndex);
    }

    private void run() {
        if (this.scheduler.getClassLoader() != null) {
            Thread.currentThread().setContextClassLoader(this.scheduler.getClassLoader());
        }
        
        var reporter = new ProblemReporter() {
            @Override
            public void reportProblem(ProblemImpl problem) {
                reportedProblems.add(problem);
            }

            @Override
            public void reportProblems(Collection<ProblemImpl> problems) {
                reportedProblems.addAll(problems);
            }
        };

        while (true) {
            try {
                var task = this.scheduler.getTask();
                if (task.isPresent()) {
                    try {
                        task.get().run(this.scheduler, reporter);
                    } catch (Exception ex) {
                        // Report as completed to avoid a deadlock where everybody waits for the failed task
                        this.scheduler.completeTask();
                        this.thrownException = ex;
                        return;
                    }

                    if (this.scheduler.completeTask()) {
                        // Finished
                        return;
                    }
                } else {
                    // Finished
                    return;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                this.thrownException = ex;
                return;
            }
        }
    }

    public void start() {
        this.thread.start();
    }

    public AnalysisResult join() throws InterruptedException {
        this.thread.join();

        if (this.thrownException == null) {
            return AnalysisResult.forSuccess(this.reportedProblems);
        } else {
            return AnalysisResult.forFailure(this.thrownException);
        }
    }
}
