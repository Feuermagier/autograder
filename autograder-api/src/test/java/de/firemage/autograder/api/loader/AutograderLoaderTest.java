package de.firemage.autograder.api.loader;

import de.firemage.autograder.api.AbstractLinter;
import de.firemage.autograder.api.AbstractTempLocation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("This test is disabled because it requires the autograder-cmd.jar to be present in the target directory, which is not the case in the CI pipeline.")
class AutograderLoaderTest {
    @Test
    void testLoadFromFile() throws IOException {
        Path path = Path.of("../autograder-cmd/target/autograder-cmd.jar");
        AutograderLoader.loadFromFile(path);
        this.assertClassesPresent();
    }

    private void assertClassesPresent() throws IOException {
        assertTrue(AutograderLoader.isAutograderLoaded());

        AbstractTempLocation randomTempLocation = AutograderLoader.instantiateTempLocation();
        randomTempLocation.close();

        AbstractTempLocation fixedTempLocation = AutograderLoader.instantiateTempLocation(Path.of(".autograder-tmp"));
        fixedTempLocation.close();

        AbstractLinter linter = AutograderLoader.instantiateLinter(AbstractLinter.builder(Locale.US));
    }
}
