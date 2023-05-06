package de.firemage.autograder.core.parallel;

import de.firemage.autograder.core.Problem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalysisScheduler {
    private final List<AnalysisThread> analysisThreads;
    private final Queue<AnalysisTask> waitingTasks;
    private volatile boolean completionAllowed;
    private AtomicInteger waitingAndRunningTaskCount;

    public AnalysisScheduler() {
        this(Runtime.getRuntime().availableProcessors() - 2);
    }

    public AnalysisScheduler(int threads) {
        this.waitingTasks = new ArrayDeque<>();
        this.completionAllowed = false;
        this.waitingAndRunningTaskCount = new AtomicInteger(0);

        this.analysisThreads = new ArrayList<>();
        int actualThreads = threads > 0 ? threads : Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
        for (int i = 0; i < actualThreads; i++) {
            var thread = new AnalysisThread(this);
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

    /**
     * Collects all problems from all threads. This method is blocking!
     * Never add more tasks *outside already submitted tasks* after calling this method, because they may never be executed.
     */
    public AnalysisResult collectProblems() {
        this.completionAllowed = true;

        List<Problem> allProblems = new ArrayList<>();
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
}
