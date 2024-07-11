package de.firemage.autograder.api.loader;

import de.firemage.autograder.api.Linter;
import de.firemage.autograder.api.TempLocation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

// @Disabled("This test is disabled because it requires the autograder-full.jar to be present in the target directory, which is not the case in the CI pipeline.")
class AutograderLoaderTest {
    @Test
    void testLoadFromFile() throws IOException {
        Path path = Path.of("../autograder-full/target/autograder-full.jar");
        AutograderLoader.loadFromFile(path);
        this.assertClassesPresent();
    }

    private void assertClassesPresent() throws IOException {
        assertTrue(AutograderLoader.isAutograderLoaded());

        TempLocation randomTempLocation = AutograderLoader.instantiateTempLocation();
        randomTempLocation.close();

        TempLocation fixedTempLocation = AutograderLoader.instantiateTempLocation(Path.of(".autograder-tmp"));
        fixedTempLocation.close();

        Linter linter = AutograderLoader.instantiateLinter(Linter.builder(Locale.US));
    }
}
