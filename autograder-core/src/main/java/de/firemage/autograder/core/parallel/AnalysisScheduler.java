package de.firemage.autograder.core.parallel;

import de.firemage.autograder.api.Problem;
import de.firemage.autograder.core.ProblemImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalysisScheduler {
    private final ClassLoader classLoader;
    private final List<AnalysisThread> analysisThreads;
    private final Queue<AnalysisTask> waitingTasks;
    private volatile boolean completionAllowed;
    private final AtomicInteger waitingAndRunningTaskCount;

    public AnalysisScheduler(int threads, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.waitingTasks = new ArrayDeque<>();
        this.completionAllowed = false;
        this.waitingAndRunningTaskCount = new AtomicInteger(0);

        this.analysisThreads = new ArrayList<>();
        int actualThreads = threads > 0 ? threads : Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);

        if (actualThreads == 1) {
            return;
        }

        for (int i = 0; i < actualThreads; i++) {
            var thread = new AnalysisThread(this, i);
            thread.start();
            this.analysisThreads.add(thread);
        }
    }

    public void submitTask(AnalysisTask task) {
        synchronized (this.waitingTasks) {
            this.waitingTasks.add(task);
            this.waitingAndRunningTaskCount.incrementAndGet();
            this.waitingTasks.notifyAll();
        }
    }

    protected Optional<AnalysisTask> getTask() throws InterruptedException {
        synchronized (this.waitingTasks) {
            while (this.waitingTasks.isEmpty()) {
                this.waitingTasks.wait(300);

                if (this.completionAllowed && this.waitingAndRunningTaskCount.get() == 0) {
                    return Optional.empty();
                }
            }
            return Optional.of(this.waitingTasks.poll());
        }
    }

    protected boolean completeTask() {
        return this.waitingAndRunningTaskCount.decrementAndGet() == 0 && this.completionAllowed;
    }

    protected ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * Collects all problems from all threads. This method is blocking!
     * Never add more tasks *outside already submitted tasks* after calling this method, because they may never be executed.
     */
    public AnalysisResult collectProblems() {
        if (this.analysisThreads.isEmpty()) {
            return executeChecksSingleThreaded();
        } else {
            return collectProblemsFromThreads();
        }
    }

    private AnalysisResult collectProblemsFromThreads() {
        this.completionAllowed = true;

        List<ProblemImpl> allProblems = new ArrayList<>();
        for (var thread : this.analysisThreads) {
            try {
                var result = thread.join();

                if (result.failed()) {
                    return result;
                }

                allProblems.addAll(result.problems());
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        return AnalysisResult.forSuccess(allProblems);
    }

    private AnalysisResult executeChecksSingleThreaded() {
        List<ProblemImpl> allProblems = new ArrayList<>();

        var reporter = new ProblemReporter() {
            @Override
            public void reportProblem(ProblemImpl problem) {
                allProblems.add(problem);
            }

            @Override
            public void reportProblems(Collection<ProblemImpl> problems) {
                allProblems.addAll(problems);
            }
        };

        while (!this.waitingTasks.isEmpty()) {
            try {
                var task = this.waitingTasks.poll();
                task.run(this, reporter);
            } catch (Exception e) {
                return AnalysisResult.forFailure(e);
            }
        }

        return AnalysisResult.forSuccess(allProblems);
    }
}
