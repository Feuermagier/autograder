package de.firemage.codelinter.core.spotbugs;

import de.firemage.codelinter.core.Problem;
import edu.umd.cs.findbugs.AbstractBugReporter;
import edu.umd.cs.findbugs.AnalysisError;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugRanker;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;

public class InCodeBugReporter extends AbstractBugReporter {
    private final BugCollection bugCollection;

    public InCodeBugReporter(Project project) {
        super.setPriorityThreshold(Confidence.LOW.getConfidenceValue());
        super.setRankThreshold(BugRanker.VISIBLE_RANK_MAX);
        bugCollection = new SortedBugCollection(project);
    }

    @Override
    protected void doReportBug(BugInstance bugInstance) {
        this.bugCollection.add(bugInstance);
    }

    @Override
    public void reportAnalysisError(AnalysisError error) {
        //TODO Don't ignore this
    }

    @Override
    public void reportMissingClass(String string) {
        //TODO Don't ignore this. Maybe throw an IllegalStateException because the code compiles?
    }

    @Override
    public void finish() {
        // Nothing to do here
    }

    @CheckForNull
    @Override
    public BugCollection getBugCollection() {
        return this.bugCollection;
    }

    @Override
    public void observeClass(ClassDescriptor classDescriptor) {
        // Nothing to do here
    }

    public List<Problem> getProblems(List<SpotbugsCheck> checks) {
        List<Problem> problems = new ArrayList<>();
        for (BugInstance bug : this.bugCollection.getCollection()) {
            for (SpotbugsCheck check : checks) {
                if (check.getBug().equals(bug.getType())) {
                    problems.add(new SpotbugsInCodeProblem(check, bug));
                }
            }
        }
        return problems;
    }
}
