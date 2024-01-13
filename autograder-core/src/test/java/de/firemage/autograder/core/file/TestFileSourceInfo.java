package de.firemage.autograder.core.file;

import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.errorprone.TempLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFileSourceInfo {
    private final TempLocation tempLocation = TempLocation.random();

    // See https://github.com/Feuermagier/autograder/issues/368
    @Test
    void testDetectThaiEncoding() throws IOException {
        try (TempLocation folder = tempLocation.createTempDirectory("test")) {
            Path folderPath = folder.tempLocation().toPath();
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
