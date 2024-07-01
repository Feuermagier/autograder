package de.firemage.autograder.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.firemage.autograder.cmd.output.Annotation;
import de.firemage.autograder.core.ArtemisUtil;
import de.firemage.autograder.core.CheckConfiguration;
import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterConfigurationException;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.span.Formatter;
import de.firemage.autograder.span.Highlight;
import de.firemage.autograder.span.Position;
import de.firemage.autograder.span.Span;
import de.firemage.autograder.span.Style;
import de.firemage.autograder.span.Text;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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

    @Option(names = {"-j", "--java", "--java-version"}, defaultValue = "17", description = "Set the Java version.")
    private String javaVersion;

    @Option(names = {
            "--artemis"}, description = "Assume that the given root folder is the workspace root of the grading tool.")
    private boolean artemisFolders;

    @Option(names = {
            "--output-json"}, description = "Output the found problems in JSON format instead of more readable plain text")
    private boolean outputJson;

    // TODO: remove this
    @Option(names = {
        "--static-only"}, description = "Only kept here so the grading tool keeps working, does nothing.")
    private boolean staticOnly;

    @Option(names = {
            "--pass-config"}, description = "Interpret the first parameter not as the path to a config file, but as the contents of the config file")
    private boolean passConfig;

    @Option(names = {"-p", "--output-pretty"}, description = "Pretty print the output", defaultValue = "false")
    private boolean isPrettyOutput;

    @Option(names = { "--max-problems" }, description = "The maximum number of problems to report per check", defaultValue = "10")
    private int maxProblemsPerCheck;

    @Spec
    private CommandSpec spec;

    private final TempLocation tempLocation;

    public Application(TempLocation tempLocation) {
        this.tempLocation = tempLocation;
    }

    private static Charset getConsoleCharset() {
        return System.console() == null ? StandardCharsets.UTF_8 : System.console().charset();
    }

    public static void main(String... args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, getConsoleCharset()));
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

    private static Highlight highlightFromCodePosition(CodePosition codePosition, String label) {
        return new Highlight(
            new Span(
                new Position(codePosition.startLine() - 1, codePosition.startColumn() - 1),
                new Position(codePosition.endLine() - 1, codePosition.endColumn() - 1)
            ),
            Optional.ofNullable(label),
            Style.ERROR
        );
    }

    private void execute(
        Linter linter,
        CheckConfiguration checkConfiguration,
        UploadedFile uploadedFile,
        Consumer<LinterStatus> statusConsumer
    ) throws LinterException, IOException {
        if (outputJson) {
            List<Problem> problems = linter.checkFile(uploadedFile, checkConfiguration, statusConsumer);
            System.out.println(">> Problems <<");
            printProblemsAsJson(problems, linter);
            return;
        }

        if (isPrettyOutput) {
            CmdUtil.beginSection("Checks");
            ProgressAnimation progress = new ProgressAnimation("Checking...");
            progress.start();
            List<Problem> problems = linter.checkFile(uploadedFile, checkConfiguration, statusConsumer);
            progress.finish("Completed checks");

            if (problems.isEmpty()) {
                CmdUtil.println("No problems found - good job!");
            } else {
                CmdUtil.println("Found " + problems.size() + " problem(s):");
                problems.stream()
                    .map(problem -> {
                        CodePosition position = problem.getPosition();
                        Text sourceText = Text.fromString(0, position.readString());
                        Formatter formatter = new Formatter(
                            System.lineSeparator(),
                            highlightFromCodePosition(position, linter.translateMessage(problem.getExplanation()))
                        );

                        String result = "[%s]: %s - Found problem in '%s'%n".formatted(
                            problem.getProblemType(),
                            problem.getCheck().getClass().getSimpleName(),
                            position.toString()
                        );
                        result += formatter.render(sourceText);

                        return result;
                    })
                    .forEach(string -> CmdUtil.println(string + System.lineSeparator()));
            }

            CmdUtil.endSection();
            return;
        }

        CmdUtil.beginSection("Checks");
        ProgressAnimation progress = new ProgressAnimation("Checking...");
        progress.start();
        List<Problem> problems = linter.checkFile(uploadedFile, checkConfiguration, statusConsumer);
        progress.finish("Completed checks");

        printProblems(problems, linter);

        CmdUtil.endSection();
    }

    @Override
    public Integer call() {
        if (!JavaVersion.isValidJavaVersion(javaVersion)) {
            throw new ParameterException(this.spec.commandLine(), "Unknown java version '" + javaVersion + "'");
        }

        if (this.artemisFolders) {
            try {
                this.file = ArtemisUtil.resolveCodePathEclipseGradingTool(this.file);
            } catch (IOException e) {
                e.printStackTrace();
                return IO_EXIT_CODE;
            }
        }

        if (!outputJson) {
            System.out.println("Student source code directory is " + file);
        }

        // Create the check configuration
        CheckConfiguration checkConfiguration;
        try {
            if (passConfig) {
                checkConfiguration = CheckConfiguration.fromConfigString(checkConfig);
            } else {
                checkConfiguration = CheckConfiguration.fromConfigFile(Path.of(checkConfig));
            }
        } catch (IOException | LinterConfigurationException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        Linter linter = Linter.builder(Locale.GERMANY)
            .threads(0)
            .tempLocation(this.tempLocation)
            .maxProblemsPerCheck(this.maxProblemsPerCheck)
            .build();

        Consumer<LinterStatus> statusConsumer = status ->
                System.out.println(linter.translateMessage(status.getMessage()));

        if (!Files.exists(file)) {
            CmdUtil.printlnErr("The path '%s' does not exist".formatted(file));
            return COMPILATION_EXIT_CODE;
        }

        try (UploadedFile uploadedFile = UploadedFile.build(
                file,
                JavaVersion.fromString(this.javaVersion),
                this.tempLocation,
                statusConsumer,
            null)) {
            this.execute(linter, checkConfiguration, uploadedFile, statusConsumer);
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
