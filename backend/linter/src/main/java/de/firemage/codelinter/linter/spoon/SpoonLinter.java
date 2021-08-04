package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.compiler.JavaVersion;
import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.spoon.check.AbstractClassWithoutChildCheck;
import de.firemage.codelinter.linter.spoon.check.AssertProcessor;
import de.firemage.codelinter.linter.spoon.check.CatchProcessor;
import de.firemage.codelinter.linter.spoon.check.Check;
import de.firemage.codelinter.linter.spoon.check.DowncastCheck;
import de.firemage.codelinter.linter.spoon.check.EmptyAbstractClassCheck;
import de.firemage.codelinter.linter.spoon.check.IllegalExitProcessor;
import de.firemage.codelinter.linter.spoon.check.LabelProcessor;
import de.firemage.codelinter.linter.spoon.check.LambdaFlowComplexityCheck;
import de.firemage.codelinter.linter.spoon.check.MethodFlowComplexityCheck;
import de.firemage.codelinter.linter.spoon.check.ObjectTypeCheck;
import de.firemage.codelinter.linter.spoon.check.UninstantiatedClassCheck;
import de.firemage.codelinter.linter.spoon.check.UnusedVariableCheck;
import de.firemage.codelinter.linter.spoon.check.VarProcessor;
import de.firemage.codelinter.linter.spoon.check.reflect.ReflectImportCheck;
import de.firemage.codelinter.linter.spoon.check.reflect.ReflectMethodCheck;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;

import java.util.List;

public class SpoonLinter {
    public List<Problem> lint(UploadedFile file, JavaVersion javaVersion) throws CompilationException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(file.getSpoonFile());
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setComplianceLevel(javaVersion.getVersionNumber());

        CtModel model;
        try {
            model = launcher.buildModel();
        } catch (ModelBuildingException e) {
            throw new CompilationException("Failed to parse the code", e);
        }
        Factory factory = launcher.getFactory();

        ProblemLogger logger = new ProblemLogger();

        Check catchProcessor = new CatchProcessor(logger);
        catchProcessor.check(model, factory);

        Check exitProcessor = new IllegalExitProcessor(logger);
        exitProcessor.check(model, factory);

        Check assertProcessor = new AssertProcessor(logger);
        assertProcessor.check(model, factory);

        Check varProcessor = new VarProcessor(logger);
        varProcessor.check(model, factory);

        Check reflectImportCheck = new ReflectImportCheck(logger);
        reflectImportCheck.check(model, factory);

        Check reflectMethodCheck = new ReflectMethodCheck(logger);
        reflectMethodCheck.check(model, factory);

        Check labelCheck = new LabelProcessor(logger);
        labelCheck.check(model, factory);

        Check methodComplexityCheck = new MethodFlowComplexityCheck(logger);
        methodComplexityCheck.check(model, factory);

        Check lambdaComplexityCheck = new LambdaFlowComplexityCheck(logger);
        lambdaComplexityCheck.check(model, factory);

        Check emptyAbstractClassCheck= new EmptyAbstractClassCheck(logger);
        emptyAbstractClassCheck.check(model, factory);

        Check uninstantiatedClassCheck = new UninstantiatedClassCheck(logger);
        uninstantiatedClassCheck.check(model, factory);

        Check abstractClassWithoutChildCheck = new AbstractClassWithoutChildCheck(logger);
        abstractClassWithoutChildCheck.check(model, factory);

        Check downcastCheck = new DowncastCheck(logger);
        downcastCheck.check(model, factory);

        Check unusedVariableCheck = new UnusedVariableCheck(logger);
        unusedVariableCheck.check(model, factory);

        Check objectUsedCheck = new ObjectTypeCheck(logger);
        objectUsedCheck.check(model, factory);

        return logger.getProblems();
    }
}
