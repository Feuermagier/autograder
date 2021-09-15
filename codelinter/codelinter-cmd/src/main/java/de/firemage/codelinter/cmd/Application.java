package de.firemage.codelinter.cmd;

import de.firemage.codelinter.core.Linter;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.compiler.CompilationDiagnostic;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.CompilationException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true, version = "codelinter-cmd 1.0",
        description = "Static code analysis for student java code")
public class Application implements Callable<Integer> {
    private static final int IO_EXIT_CODE = 3;
    private static final int COMPILATION_EXIT_CODE = 4;
    private static final int MISC_EXIT_CODE = 10;

    private static final int CAPTION_LENGTH = 20;

    @Parameters(index = "0", description = "The root folder which contains the files to check")
    private File file;
    @Option(names = {"-p", "--pmd"}, description = "Enable PMD checks.")
    private boolean enablePMD;
    @Option(names = {"-s", "--spotbugs"}, description = "Enable SpotBugs checks.")
    private boolean enableSpotBugs;
    @Option(names = {"-cpd", "--copy-paste-detection"}, description = "Enable copy-paste detection.")
    private boolean enableCPD;
    @Option(names = {"-j", "--java", "--java-version"}, defaultValue = "11", description = "Set the Java version.")
    private String javaVersion;
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

        try (Linter linter = new Linter(new UploadedFile(file))) {
            try {
                CmdUtil.beginSection("Compilation");
                ProgressAnimation progress = new ProgressAnimation("Compiling...");
                progress.start();
                List<CompilationDiagnostic> diagnostics = linter.compile(JavaVersion.fromString(javaVersion), getTmpDirectory());
                progress.finish("Completed compilation");
                diagnostics.forEach(d -> CmdUtil.println(d.toString()));
                CmdUtil.endSection();
            } catch (CompilationFailureException e) {
                CmdUtil.println(e.getMessage());
                return COMPILATION_EXIT_CODE;
            }

            try {
                CmdUtil.beginSection("Custom Lints");
                ProgressAnimation progress = new ProgressAnimation("Executing custom lints...");
                progress.start();
                List<Problem> problems = linter.executeSpoonLints(JavaVersion.fromString(javaVersion));
                progress.finish("Completed custom lints");
                printProblems(problems);
                CmdUtil.endSection();
            } catch (CompilationException e) {
                CmdUtil.printlnErr(e.getMessage());
                return COMPILATION_EXIT_CODE;
            }

            if (enableCPD) {
                CmdUtil.beginSection("Copy/Paste Detection");
                ProgressAnimation progress = new ProgressAnimation("Executing CPD...");
                progress.start();
                List<Problem> problems = linter.executeCPDLints();
                progress.finish("Completed CPD");
                printProblems(problems);
                CmdUtil.endSection();
            }

            if (enablePMD) {
                CmdUtil.beginSection("PMD");
                ProgressAnimation progress = new ProgressAnimation("Executing PMD...");
                progress.start();
                List<Problem> problems = linter.executePMDLints(Paths.get("config/ruleset.xml"));
                progress.finish("Completed PMD analysis");
                printProblems(problems);
                CmdUtil.endSection();
            }

            if (enableSpotBugs) {
                try {
                    CmdUtil.beginSection("SpotBugs");
                    ProgressAnimation progress = new ProgressAnimation("Executing SpotBugs...");
                    progress.start();
                    List<Problem> problems = linter.executeSpotbugsLints();
                    progress.finish("Completed SpotBugs analysis");
                    printProblems(problems);
                    CmdUtil.endSection();
                } catch (InterruptedException e) {
                    CmdUtil.printlnErr(e.getMessage());
                    return MISC_EXIT_CODE;
                }
            }
        } catch (IOException e) {
            CmdUtil.printlnErr(e.getMessage());
            return IO_EXIT_CODE;
        }

        return 0;
    }

    private File getTmpDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    private void printProblems(List<Problem> problems) {
        if (problems.isEmpty()) {
            CmdUtil.println("No problems found - good job!");
        } else {
            CmdUtil.println("Found " + problems.size() + " problem(s):");
            problems.forEach(p -> CmdUtil.println(p.toString()));
        }
    }
}
