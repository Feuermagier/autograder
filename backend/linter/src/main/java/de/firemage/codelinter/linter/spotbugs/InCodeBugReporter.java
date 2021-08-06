package de.firemage.codelinter.linter.spotbugs;

import de.firemage.codelinter.linter.Problem;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InCodeBugReporter extends AbstractBugReporter {
    private final List<Problem> problems = new ArrayList<>();
    private final BugCollection bugCollection;
    private final File root;

    public InCodeBugReporter(File root, Project project) {
        this.root = root;
        super.setPriorityThreshold(Confidence.LOW.getConfidenceValue());
        super.setRankThreshold(BugRanker.VISIBLE_RANK_MAX);
        bugCollection = new SortedBugCollection(project);
    }

    @Override
    protected void doReportBug(BugInstance bugInstance) {
        this.bugCollection.add(bugInstance);
        if (!Confidence.getConfidence(bugInstance.getPriority()).equals(Confidence.IGNORE)) {
            problems.add(new SpotbugsInCodeProblem(bugInstance, this.root));
        }
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

    public List<Problem> getProblems() {
        return this.bugCollection.getCollection().stream()
                .map(b -> new SpotbugsInCodeProblem(b, this.root))
                .collect(Collectors.toList());
        //return Collections.unmodifiableList(this.problems);
    }
}
