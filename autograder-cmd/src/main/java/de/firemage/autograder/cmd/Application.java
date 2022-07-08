package de.firemage.autograder.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.firemage.autograder.cmd.config.CheckConfig;
import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.UploadedFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

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
    @Option(names = {"-j", "--java", "--java-version"}, defaultValue = "11", description = "Set the Java version.")
    private String javaVersion;
    @Option(names = {"-s", "--static-only"}, description = "Only run static analysis, therefore disabling dynamic analysis.")
    private boolean staticOnly;
    @Spec
    private CommandSpec spec;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {

        if (!JavaVersion.isValidJavaVersion(javaVersion)) {
            throw new ParameterException(this.spec.commandLine(), "Unknown java version '" + javaVersion + "'");
        }

        boolean dynamic = !this.staticOnly && !this.tests.toString().equals("");
        if (!dynamic) {
            CmdUtil.println("Note: Dynamic analysis is disabled.");
            CmdUtil.println();
        }

        List<Check> checks;
        try {
            CheckConfig config = new ObjectMapper(new YAMLFactory()).readValue(checkConfig.toFile(), CheckConfig.class);
            checks = config.getChecks().stream().flatMap(c -> c.create().stream()).toList();
        } catch (IOException e) {
            e.printStackTrace();
            return IO_EXIT_CODE;
        }

        Linter linter = new Linter();
        UploadedFile uploadedFile = new UploadedFile(file, JavaVersion.fromString(this.javaVersion));

        try {
            CmdUtil.beginSection("Checks");
            ProgressAnimation progress = new ProgressAnimation("Checking...");
            progress.start();
            List<Problem> problems =
                linter.checkFile(uploadedFile, getTmpDirectory(), tests, checks, progress::updateText, !dynamic);
            progress.finish("Completed checks");
            printProblems(problems);
            CmdUtil.endSection();
        } catch (CompilationFailureException e) {
            CmdUtil.println(e.getMessage());
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

    private void printProblems(List<Problem> problems) {
        if (problems.isEmpty()) {
            CmdUtil.println("No problems found - good job!");
        } else {
            CmdUtil.println("Found " + problems.size() + " problem(s):");
            problems.stream().map(this::formatProblem).sorted().forEach(CmdUtil::println);
        }
    }

    private String formatProblem(Problem problem) {
        return String.format("%s %s (Source: %s)",
            problem.getDisplayLocation(),
            problem.getExplanation(),
            problem.getCheck().getLinter());
    }
}
