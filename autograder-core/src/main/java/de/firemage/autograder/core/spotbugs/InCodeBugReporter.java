package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.SourceInfo;
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
    private final SourceInfo sourceInfo;

    public InCodeBugReporter(Project project, SourceInfo sourceInfo) {
        super.setPriorityThreshold(Confidence.LOW.getConfidenceValue());
        super.setRankThreshold(BugRanker.VISIBLE_RANK_MAX);
        bugCollection = new SortedBugCollection(project);
        this.sourceInfo = sourceInfo;
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
                    problems.add(new SpotbugsInCodeProblem(check, bug, this.sourceInfo));
                }
            }
        }
        return problems;
    }
}
