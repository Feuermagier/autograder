package de.firemage.autograder.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.firemage.autograder.cmd.output.Annotation;
import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.errorprone.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Command(mixinStandardHelpOptions = true, version = "codelinter-cmd 1.0",
        description = "Static code analysis for student java code")
public class Application implements Callable<Integer> {
    private static final int IO_EXIT_CODE = 3;
    private static final int COMPILATION_EXIT_CODE = 4;
    private static final int MISC_EXIT_CODE = 10;

    private static final int CAPTION_LENGTH = 20;

    @Parameters(index = "0", description = "The check configuration.")
    private String checkConfig;

    @Parameters(index = "1", description = "The root folder which contains the files to check.")
    private Path file;

    @Parameters(index = "2", defaultValue = "", description = "The root folder which contains the tests to run. If not provided or empty, no tests will be run.")
    private Path tests;

    @Option(names = {"-j", "--java", "--java-version"}, defaultValue = "17", description = "Set the Java version.")
    private String javaVersion;

    @Option(names = {"-s",
            "--static-only"}, description = "Only run static analysis, therefore disabling dynamic analysis.")
    private boolean staticOnly;

    @Option(names = {
            "--artemis"}, description = "Assume that the given root folder is the workspace root of the grading tool.")
    private boolean artemisFolders;

    @Option(names = {
            "--output-json"}, description = "Output the found problems in JSON format instead of more readable plain text")
    private boolean outputJson;

    @Option(names = {
            "--pass-config"}, description = "Interpret the first parameter not as the path to a config file, but as the contents of the config file")
    private boolean passConfig;

    @Spec
    private CommandSpec spec;

    private final TempLocation tempLocation;

    public Application(TempLocation tempLocation) {
        this.tempLocation = tempLocation;
    }

    public static void main(String... args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        int exitCode = runApplication(args);
        System.exit(exitCode);
    }

    // Useful for testing
    public static int runApplication(String... args) {
        // to automatically delete the temp location on exit
        try (TempLocation tempLocation = TempLocation.of(".autograder-tmp")) {
            return new CommandLine(new Application(tempLocation)).execute(args);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not create temp location", exception);
        }
    }

    @Override
    public Integer call() {
        if (!JavaVersion.isValidJavaVersion(javaVersion)) {
            throw new ParameterException(this.spec.commandLine(), "Unknown java version '" + javaVersion + "'");
        }

        if (this.artemisFolders) {
            try (Stream<Path> files = Files.list(this.file)) {
                this.file = files
                        .filter(child -> !child.endsWith(".metadata"))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("No student code found"))
                        .resolve("assignment")
                        .resolve("src");
            } catch (IOException e) {
                e.printStackTrace();
                return IO_EXIT_CODE;
            }
        }

        if (!outputJson) {
            System.out.println("Student source code directory is " + file);
        }

        boolean isDynamicAnalysisEnabled = !this.staticOnly && !this.tests.toString().equals("");
        if (!isDynamicAnalysisEnabled && !outputJson) {
            CmdUtil.println("Note: Dynamic analysis is disabled.");
            CmdUtil.println();
        }

        List<ProblemType> checks;
        try {
            if (passConfig) {
                checks = List.of(new ObjectMapper(new YAMLFactory()).readValue(checkConfig, ProblemType[].class));
            } else {
                checks = List.of(new ObjectMapper(new YAMLFactory()).readValue(new File(checkConfig), ProblemType[].class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        Linter linter = Linter.builder(Locale.GERMANY)
            .threads(0)
            .tempLocation(this.tempLocation)
            .enableDynamicAnalysis(isDynamicAnalysisEnabled)
            .maxProblemsPerCheck(10)
            .build();

        Consumer<LinterStatus> statusConsumer = status ->
                System.out.println(linter.translateMessage(status.getMessage()));

        try (UploadedFile uploadedFile = UploadedFile.build(
                file,
                JavaVersion.fromString(this.javaVersion),
                this.tempLocation,
                statusConsumer,
            null)) {

            if (outputJson) {
                List<Problem> problems = linter.checkFile(uploadedFile, tests, checks, statusConsumer);
                System.out.println(">> Problems <<");
                printProblemsAsJson(problems, linter);
            } else {
                CmdUtil.beginSection("Checks");
                ProgressAnimation progress = new ProgressAnimation("Checking...");
                progress.start();
                List<Problem> problems = linter.checkFile(uploadedFile, tests, checks, statusConsumer);
                progress.finish("Completed checks");

                printProblems(problems, linter);

                CmdUtil.endSection();
            }
        } catch (CompilationFailureException e) {
            CmdUtil.printlnErr("Compilation failed: " + e.getMessage());
            return COMPILATION_EXIT_CODE;
        } catch (LinterException e) {
            e.printStackTrace();
            return MISC_EXIT_CODE;
        } catch (IOException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        return 0;
    }

    private void printProblems(List<Problem> problems, Linter linter) {
        if (problems.isEmpty()) {
            CmdUtil.println("No problems found - good job!");
        } else {
            CmdUtil.println("Found " + problems.size() + " problem(s):");
            problems.stream().map(p -> this.formatProblem(p, linter)).sorted().forEach(CmdUtil::println);
        }
    }

    private void printProblemsAsJson(Collection<? extends Problem> problems, Linter linter) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonOutput = mapper.writeValueAsString(problems.stream().map(problem -> {
                CodePosition position = problem.getPosition();
                return new Annotation(
                    problem.getProblemType(),
                    linter.translateMessage(problem.getExplanation()),
                    position.file().toString().replace("\\", "/"),
                    position.startLine(),
                    position.endLine()
                );
            }).toList());
            System.out.println(jsonOutput);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    private String formatProblem(Problem problem, Linter linter) {
        return String.format("%s %s (Source: %s)",
                problem.getDisplayLocation(),
                linter.translateMessage(problem.getExplanation()),
                linter.translateMessage(problem.getCheck().getLinter()));
    }
}
