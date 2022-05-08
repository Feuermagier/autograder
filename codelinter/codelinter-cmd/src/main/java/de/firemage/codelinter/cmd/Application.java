package de.firemage.codelinter.cmd;

import de.firemage.codelinter.core.Linter;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.check.ForToForEachCheck;
import de.firemage.codelinter.core.check.complexity.DiamondOperatorCheck;
import de.firemage.codelinter.core.check.complexity.ForLoopVariableCheck;
import de.firemage.codelinter.core.check.complexity.RedundantConstructorCheck;
import de.firemage.codelinter.core.check.naming.LinguisticNamingCheck;
import de.firemage.codelinter.core.check.oop.ConcreteCollectionCheck;
import de.firemage.codelinter.core.check.oop.MethodShouldBeAbstractCheck;
import de.firemage.codelinter.core.check.structure.DefaultPackageCheck;
import de.firemage.codelinter.core.check.unnecessary.EmptyNonCatchBlockCheck;
import de.firemage.codelinter.core.check.unnecessary.UnusedCodeElementCheck;
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

    @Parameters(index = "0", description = "The root folder which contains the files to check")
    private Path file;
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

        Linter linter = new Linter();
        UploadedFile uploadedFile = new UploadedFile(file, JavaVersion.fromString(this.javaVersion));

        try {
            CmdUtil.beginSection("Checks");
            ProgressAnimation progress = new ProgressAnimation("Checking...");
            progress.start();
            List<Problem> problems = linter.checkFile(uploadedFile, getTmpDirectory(), List.of(
                    new MethodShouldBeAbstractCheck()
            ));
            progress.finish("Completed checks");
            printProblems(problems);
            CmdUtil.endSection();
        } catch (CompilationFailureException e) {
            CmdUtil.println(e.getMessage());
            return COMPILATION_EXIT_CODE;
        } catch (CompilationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Path getTmpDirectory() {
        return new File(System.getProperty("java.io.tmpdir")).toPath();
    }

    private void printProblems(List<Problem> problems) {
        if (problems.isEmpty()) {
            CmdUtil.println("No problems found - good job!");
        } else {
            CmdUtil.println("Found " + problems.size() + " problem(s):");
            problems.forEach(p -> CmdUtil.println(p.displayAsString(this.file.toAbsolutePath())));
        }
    }
}
