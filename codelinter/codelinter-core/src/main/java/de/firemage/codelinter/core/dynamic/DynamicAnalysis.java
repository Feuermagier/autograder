package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.event.Event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DynamicAnalysis {
    private static final int TIMEOUT_SECONDS = 10;
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
        String[] command = new String[] {
            "java",
            "-cp",
            this.jar.toAbsolutePath().toString(),
            "-javaagent:\"" + this.agent.toAbsolutePath() + "\"=\"" + outPath.toAbsolutePath() + "\"",
            this.mainClass.replace("/", ".")
        };
        Process container = Runtime.getRuntime().exec(command);
        if (!container.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            container.destroyForcibly();
            throw new IllegalStateException("Child JVM timed out");
        }
        if (container.exitValue() != 0) {
            System.out.println(new String(container.getErrorStream().readAllBytes()));
            throw new IllegalStateException("Child JVM exited with nonzero exit code " + container.exitValue());
        }

        List<Event> events = Event.read(outPath);
        Files.deleteIfExists(outPath);
        return events;
    }
}
