package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.UploadedFile;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.config.UserPreferences;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SpotbugsLinter {
    public List<Problem> lint(UploadedFile file, Path jar, List<SpotbugsCheck> checks) throws IOException, InterruptedException {
        try (Project project = new Project()) {
            project.addFile(jar.toAbsolutePath().toString());
            InCodeBugReporter reporter = new InCodeBugReporter(project, file.getSource());

            try (FindBugs2 findBugs = new FindBugs2()) {
                findBugs.setBugReporter(reporter);
                findBugs.setProject(project);

                UserPreferences userPreferences = UserPreferences.createDefaultUserPreferences();
                userPreferences.setEffort(UserPreferences.EFFORT_DEFAULT);
                userPreferences.enableAllDetectors(true);
                // Disable debug detectors (these are spamming System.out)
                DetectorFactoryCollection collection = DetectorFactoryCollection.instance();
                userPreferences.enableDetector(collection.getFactory("TestingGround"), false);
                userPreferences.enableDetector(collection.getFactory("CheckCalls"), false);
                userPreferences.enableDetector(collection.getFactory("Noise"), false);
                userPreferences.enableDetector(collection.getFactory("ViewCFG"), false);
                userPreferences.getFilterSettings().clearAllCategories();

                findBugs.setDetectorFactoryCollection(collection);
                findBugs.setUserPreferences(userPreferences);
                findBugs.finishSettings();
                findBugs.execute();

                return reporter.getProblems(checks);
            }
        }

    }
}
