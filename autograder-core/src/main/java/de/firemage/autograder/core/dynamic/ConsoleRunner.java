package de.firemage.autograder.core.dynamic;

import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.event.Event;
import spoon.reflect.declaration.CtClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleRunner implements TestRunner {
    private static final int TIMEOUT_SECONDS = 10;
    private final Path tmpDirectory;
    private final Path agent;
    private final Path tests;

    public ConsoleRunner(Path tmpDirectory, Path agent, Path tests) {
        this.tmpDirectory = tmpDirectory;
        this.agent = agent;
        if (!Files.isDirectory(tests)) {
            throw new IllegalArgumentException("tests must point to a folder containing the individual test cases");
        }
        this.tests = tests;
    }

    public List<List<Event>> runTests(StaticAnalysis analysis, Path jar) throws IOException, InterruptedException {
        List<Path> testCases;
        try (Stream<Path> files = Files.list(this.tests)) {
            testCases = files.filter(Files::isRegularFile).toList();
        }
        String mainClass = analysis.getCodeModel().findMain().getParent(CtClass.class).getQualifiedName().replace(".", "/");

        List<List<Event>> events = new ArrayList<>();
        for (Path testPath : testCases) {
            events.add(executeTestCase(testPath, mainClass, jar));
        }

        return events;
    }

    private List<Event> executeTestCase(Path testFile, String mainClass, Path jar)
        throws IOException, InterruptedException {
        Path outPath = this.tmpDirectory.resolve("codelinter_events.txt");

        List<String> lines = Files.readAllLines(testFile);
        Process container = startJVM(mainClass, jar, outPath);

        BufferedReader containerOut = container.inputReader();
        BufferedWriter containerIn = container.outputWriter();

        List<String> expectedOutput = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("> ")) {
                containerIn.write(line.substring(2));
                containerIn.newLine();
            } else {
                expectedOutput.add(line);
            }
        }
        containerIn.flush();

        if (!container.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            container.destroyForcibly();
        }

        if (container.exitValue() != 0) {
            System.out.println(containerOut.lines().collect(Collectors.joining("\n")));
            throw new IllegalStateException("Child JVM exited with nonzero exit code " + container.exitValue());
        }

        int outputLine = 0;
        while (containerOut.ready()) {
            String actualOutput = containerOut.readLine();
            if (actualOutput.startsWith("AGENT: ")) {
                continue;
            }
            String expected = expectedOutput.get(outputLine);
            if (expected.startsWith("Error, ")) {
                if (!actualOutput.startsWith("Error, ")) {
                    throw new IllegalStateException("Expected an error output but got '" + actualOutput + "'");
                }
            } else if (!expected.equals(actualOutput)) {
                throw new IllegalStateException(
                    "Expected output '" + expected + "', got output '" + actualOutput + "'");
            }
            outputLine++;
        }

        if (outputLine < expectedOutput.size()) {
            throw new IllegalStateException(
                "There are " + (expectedOutput.size() - outputLine) + "lines left in the expected output");
        }

        List<Event> events = Event.read(outPath);
        Files.deleteIfExists(outPath);
        return events;
    }

    private Process startJVM(String mainClass, Path jar, Path outPath) throws IOException {
        String[] command = new String[] {
            "java",
            "-cp",
            jar.toAbsolutePath().toString(),
            "-javaagent:\"" + this.agent.toAbsolutePath() + "\"=\"" + outPath.toAbsolutePath() + "\"",
            mainClass.replace("/", ".")
        };
        return Runtime.getRuntime().exec(command);
    }
}
