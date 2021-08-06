package de.firemage.codelinter.linter.spotbugs;

import de.firemage.codelinter.linter.Problem;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.PrintingBugReporter;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SystemProperties;
import edu.umd.cs.findbugs.config.AnalysisFeatureSetting;
import edu.umd.cs.findbugs.config.UserPreferences;
import edu.umd.cs.findbugs.detect.TestingGround;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class SpotbugsLinter {
    public List<Problem> lint(File jar) throws IOException, InterruptedException {
        Project project = new Project();
        project.addFile(jar.getAbsolutePath());

        InCodeBugReporter reporter = new InCodeBugReporter(jar, project);
        FindBugs2 findBugs = new FindBugs2();
        findBugs.setBugReporter(reporter);
        findBugs.setProject(project);
        UserPreferences userPreferences = UserPreferences.createDefaultUserPreferences();
        userPreferences.setEffort(UserPreferences.EFFORT_MAX);
        userPreferences.enableAllDetectors(true);

        // Disable debug detectors (those are using System.out for spamming)
        userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory("TestingGround"), false);
        userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory("CheckCalls"), false);

        userPreferences.getFilterSettings().clearAllCategories();
        findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        findBugs.setUserPreferences(userPreferences);
        findBugs.finishSettings();

        findBugs.execute();
        findBugs.close();

        return reporter.getProblems();
    }
}
