package de.firemage.codelinter.core.spotbugs;

import de.firemage.codelinter.core.Problem;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.config.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SpotbugsLinter {
    public List<Problem> lint(Path jar) throws IOException, InterruptedException {
        try (Project project = new Project()) {
            project.addFile(jar.toAbsolutePath().toFile().toString());
            InCodeBugReporter reporter = new InCodeBugReporter(project);

            try (FindBugs2 findBugs = new FindBugs2()) {
                findBugs.setBugReporter(reporter);
                findBugs.setProject(project);

                UserPreferences userPreferences = UserPreferences.createDefaultUserPreferences();
                userPreferences.setEffort(UserPreferences.EFFORT_DEFAULT);
                userPreferences.enableAllDetectors(true);
                //userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory(), );
                // Disable debug detectors (these are spamming System.out)
                userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory("TestingGround"), false);
                userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory("CheckCalls"), false);
                userPreferences.enableDetector(DetectorFactoryCollection.instance().getFactory("Noise"), false);
                userPreferences.getFilterSettings().clearAllCategories();

                findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
                findBugs.setUserPreferences(userPreferences);
                findBugs.finishSettings();
                findBugs.execute();

                return reporter.getProblems();
            }
        }

    }
}
