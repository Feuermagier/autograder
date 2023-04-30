package de.firemage.autograder.core.parallel;

import de.firemage.autograder.core.Problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnalysisThread {
    private final AnalysisScheduler scheduler;
    private final Thread thread;
    private final List<Problem> reportedProblems;
    private Exception thrownException;

    public AnalysisThread(AnalysisScheduler scheduler) {
        this.scheduler = scheduler;
        this.reportedProblems = new ArrayList<>();
        this.thread = new Thread(this::run);
    }

    private void run() {
        var reporter = new ProblemReporter() {
            @Override
            public void reportProblem(Problem problem) {
                reportedProblems.add(problem);
            }

            @Override
            public void reportProblems(Collection<Problem> problems) {
                reportedProblems.addAll(problems);
            }
        };

        while (true) {
            try {
                var task = this.scheduler.getTask();
                if (task.isPresent()) {
                    task.get().run(this.scheduler, reporter);

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
            } catch (Exception ex) {
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
