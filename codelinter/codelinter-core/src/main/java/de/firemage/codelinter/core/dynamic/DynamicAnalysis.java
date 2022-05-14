package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.event.Event;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DynamicAnalysis {
    private static final int TIMEOUT_SECONDS = 10;
    private static final String COMMAND_PATTERN = "java -cp \"%s\" -javaagent:\"%s\"=\"%s\" %s";
    private final Path tmpDirectory;
    private final Path jar;
    private final Path agent;
    private final String mainClass;

    public DynamicAnalysis(Path tmpDirectory, Path jar, Path agent, String mainClass) {
        this.tmpDirectory = tmpDirectory;
        this.jar = jar;
        this.agent = agent;
        this.mainClass = mainClass;
    }

    public List<Event> run() throws IOException, InterruptedException {
        Path outPath = this.tmpDirectory.resolve("codelinter_events.txt");
        String command = String.format(COMMAND_PATTERN, this.jar, this.agent, outPath, this.mainClass.replace("/", "."));
        Process container = Runtime.getRuntime().exec(command);
        if (!container.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            container.destroyForcibly();
        }
        return Event.read(outPath);
    }
}
