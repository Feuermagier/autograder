package de.firemage.autograder.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.firemage.autograder.cmd.output.Annotation;
import de.firemage.autograder.core.InCodeProblem;
import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.visualize.dot.DotGraph;
import de.firemage.autograder.core.visualize.structure.StructureVisualizer;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Path checkConfig;
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

    @Spec
    private CommandSpec spec;

    public static void main(String... args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (!JavaVersion.isValidJavaVersion(javaVersion)) {
            throw new ParameterException(this.spec.commandLine(), "Unknown java version '" + javaVersion + "'");
        }

        if (artemisFolders) {
            try {
                file = Files.list(file)
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

        boolean dynamic = !this.staticOnly && !this.tests.toString().equals("");
        if (!dynamic && !outputJson) {
            CmdUtil.println("Note: Dynamic analysis is disabled.");
            CmdUtil.println();
        }

        List<ProblemType> checks;

        try {
            checks = List.of(new ObjectMapper(new YAMLFactory()).readValue(checkConfig.toFile(), ProblemType[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        Linter linter = new Linter(Locale.GERMANY);
        Consumer<LinterStatus> statusConsumer = status ->
            System.out.println(linter.translateMessage(status.getMessage()));

        try {
            UploadedFile uploadedFile = UploadedFile.build(file,
                JavaVersion.fromString(this.javaVersion), getTmpDirectory(), statusConsumer);
            
            if (outputJson) {
                List<Problem> problems =
                    linter.checkFile(uploadedFile, getTmpDirectory(), tests, checks, statusConsumer, !dynamic);
                System.out.println(">> Problems <<");
                printProblemsAsJson(problems, linter);
            } else {
                CmdUtil.beginSection("Checks");
                ProgressAnimation progress = new ProgressAnimation("Checking...");
                progress.start();
                List<Problem> problems =
                    linter.checkFile(uploadedFile, getTmpDirectory(), tests, checks, statusConsumer, !dynamic);
                progress.finish("Completed checks");

                printProblems(problems, linter);

                CmdUtil.endSection();
            }
        } catch (CompilationFailureException e) {
            CmdUtil.printlnErr("Compilation failed: " + e.getMessage());
            return COMPILATION_EXIT_CODE;
        } catch (LinterException | InterruptedException e) {
            e.printStackTrace();
            return MISC_EXIT_CODE;
        } catch (IOException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        return 0;
    }

    private Path getTmpDirectory() {
        //return new File(System.getProperty("java.io.tmpdir")).toPath();
        return Path.of("tmp");
    }

    private void printProblems(List<Problem> problems, Linter linter) {
        if (problems.isEmpty()) {
            CmdUtil.println("No problems found - good job!");
        } else {
            CmdUtil.println("Found " + problems.size() + " problem(s):");
            problems.stream().map(p -> this.formatProblem(p, linter)).sorted().forEach(CmdUtil::println);
        }
    }

    private void printProblemsAsJson(List<Problem> problems, Linter linter) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonOutput = mapper.writeValueAsString(problems.stream().map(p -> {
                if (p instanceof InCodeProblem inCodeProblem) {
                    var position = inCodeProblem.getPosition();
                    return Optional.of(new Annotation(inCodeProblem.getProblemType(),
                        linter.translateMessage(inCodeProblem.getExplanation()),
                        position.file().toString().replace("\\", "/"), position.startLine(), position.endLine()));
                } else {
                    return Optional.empty();
                }
            }).filter(Optional::isPresent).map(Optional::get).toList());
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
