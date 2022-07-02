package de.firemage.codelinter.cmd;

import de.firemage.codelinter.core.Linter;
import de.firemage.codelinter.core.LinterException;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.check.Check;
import de.firemage.codelinter.core.check.CompareObjectsNotStringsCheck;
import de.firemage.codelinter.core.check.ConstantNamingAndQualifierCheck;
import de.firemage.codelinter.core.check.ConstantsInInterfaceCheck;
import de.firemage.codelinter.core.check.CopyPasteCheck;
import de.firemage.codelinter.core.check.DontReassignParametersCheck;
import de.firemage.codelinter.core.check.DoubleBraceInitializationCheck;
import de.firemage.codelinter.core.check.EqualsContractCheck;
import de.firemage.codelinter.core.check.FieldShouldBeLocalCheck;
import de.firemage.codelinter.core.check.ForToForEachCheck;
import de.firemage.codelinter.core.check.MissingOverrideAnnotationCheck;
import de.firemage.codelinter.core.check.api.IsEmptyReimplementationCheck;
import de.firemage.codelinter.core.check.api.OldCollectionCheck;
import de.firemage.codelinter.core.check.api.StringIsEmptyReimplementationCheck;
import de.firemage.codelinter.core.check.comment.AuthorTagCheck;
import de.firemage.codelinter.core.check.comment.CommentLanguageCheck;
import de.firemage.codelinter.core.check.comment.CommentedOutCodeCheck;
import de.firemage.codelinter.core.check.comment.JavadocParamCheck;
import de.firemage.codelinter.core.check.comment.JavadocReturnNullCheck;
import de.firemage.codelinter.core.check.comment.JavadocStubCheck;
import de.firemage.codelinter.core.check.complexity.DiamondOperatorCheck;
import de.firemage.codelinter.core.check.complexity.ExtendsObjectCheck;
import de.firemage.codelinter.core.check.complexity.ForLoopVariableCheck;
import de.firemage.codelinter.core.check.complexity.RedundantModifierCheck;
import de.firemage.codelinter.core.check.complexity.RedundantReturnCheck;
import de.firemage.codelinter.core.check.complexity.UnnecessaryLocalBeforeReturnCheck;
import de.firemage.codelinter.core.check.complexity.UnusedImportCheck;
import de.firemage.codelinter.core.check.complexity.WrapperInstantiationCheck;
import de.firemage.codelinter.core.check.debug.AssertCheck;
import de.firemage.codelinter.core.check.debug.PrintStackTraceCheck;
import de.firemage.codelinter.core.check.exceptions.EmptyCatchCheck;
import de.firemage.codelinter.core.check.naming.BooleanMethodNameCheck;
import de.firemage.codelinter.core.check.naming.LinguisticNamingCheck;
import de.firemage.codelinter.core.check.naming.VariablesHaveDescriptiveNamesCheck;
import de.firemage.codelinter.core.check.oop.ConcreteCollectionCheck;
import de.firemage.codelinter.core.check.oop.ListGetterSetterCheck;
import de.firemage.codelinter.core.check.oop.MethodShouldBeAbstractCheck;
import de.firemage.codelinter.core.check.structure.DefaultPackageCheck;
import de.firemage.codelinter.core.check.unnecessary.EmptyNonCatchBlockCheck;
import de.firemage.codelinter.core.check.unnecessary.UnusedCodeElementCheck;
import de.firemage.codelinter.core.compiler.CompilationFailureException;
import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.file.UploadedFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import java.io.IOException;
import java.net.URISyntaxException;
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
    @Parameters(index = "1", description = "The root folder which contains the tests to run")
    private Path tests;
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
            List<Problem> problems =
                    linter.checkFile(uploadedFile, getTmpDirectory(), tests, getChecks(), progress::updateText, false);
            progress.finish("Completed checks");
            printProblems(problems);
            CmdUtil.endSection();
        } catch (CompilationFailureException e) {
            CmdUtil.println(e.getMessage());
            return COMPILATION_EXIT_CODE;
        } catch (LinterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private List<Check> getChecks() {
        return List.of(
                // General
                new ConstantsInInterfaceCheck(false),
                new CopyPasteCheck(100),
                new DoubleBraceInitializationCheck(),
                new ForToForEachCheck(),
                new MissingOverrideAnnotationCheck(),
                new EqualsContractCheck(),
                new CompareObjectsNotStringsCheck(),
                new ConstantNamingAndQualifierCheck(),
                new DontReassignParametersCheck(),
                new FieldShouldBeLocalCheck(),
                // API
                new IsEmptyReimplementationCheck(),
                new OldCollectionCheck(),
                new StringIsEmptyReimplementationCheck(),
                // Complexity
                new DiamondOperatorCheck(),
                new ExtendsObjectCheck(),
                new ForLoopVariableCheck(),
                //new RedundantConstructorCheck(), // Allow declaring empty constructors for documentation
                new RedundantModifierCheck(),
                new RedundantReturnCheck(),
                new UnnecessaryLocalBeforeReturnCheck(),
                new UnusedImportCheck(),
                new WrapperInstantiationCheck(),
                // Debug
                new AssertCheck(),
                new PrintStackTraceCheck(),
                // Exceptions
                new EmptyCatchCheck(),
                // Comments
                new JavadocReturnNullCheck(),
                new CommentLanguageCheck(),
                new JavadocStubCheck(),
                new VariablesHaveDescriptiveNamesCheck(),
                new CommentedOutCodeCheck(),
                new AuthorTagCheck("u(\\w){4}"),
                new JavadocParamCheck(),
                // Naming
                new BooleanMethodNameCheck(),
                new LinguisticNamingCheck(),
                // OOP
                new ConcreteCollectionCheck(),
                new MethodShouldBeAbstractCheck(),
                new ListGetterSetterCheck(),
                // Structure
                new DefaultPackageCheck(),
                // Unnecessary
                new EmptyNonCatchBlockCheck(),
                new UnusedCodeElementCheck()
        );
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
