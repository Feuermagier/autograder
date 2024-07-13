package de.firemage.autograder.core.file;

import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.api.AbstractTempLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFileSourceInfo {
    private final AbstractTempLocation tempLocation = TempLocation.random();

    // See https://github.com/Feuermagier/autograder/issues/368
    @Test
    void testDetectThaiEncoding() throws IOException {
        try (AbstractTempLocation folder = tempLocation.createTempDirectory("test")) {
            Path folderPath = folder.toPath();
            Path filePath = Paths.get(folderPath.toString(), "Test.java");
            Files.write(filePath, "public class Test { char symbol = '§'; }".getBytes());

            FileSourceInfo sourceInfo = new FileSourceInfo(folderPath, JavaVersion.JAVA_17);

            assertEquals(1, sourceInfo.compilationUnits().size());
            for (CompilationUnit unit : sourceInfo.compilationUnits()) {
                assertEquals(StandardCharsets.UTF_8, unit.charset());
            }
        }
    }
}
