package de.firemage.codelinter.core.spoon;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.check.AbstractClassWithoutChildCheck;
import de.firemage.codelinter.core.spoon.check.AssertProcessor;
import de.firemage.codelinter.core.spoon.check.CatchProcessor;
import de.firemage.codelinter.core.spoon.check.Check;
import de.firemage.codelinter.core.spoon.check.DowncastCheck;
import de.firemage.codelinter.core.spoon.check.EmptyAbstractClassCheck;
import de.firemage.codelinter.core.spoon.check.ForEachProcessor;
import de.firemage.codelinter.core.spoon.check.IllegalExitProcessor;
import de.firemage.codelinter.core.spoon.check.LabelProcessor;
import de.firemage.codelinter.core.spoon.check.LambdaFlowComplexityCheck;
import de.firemage.codelinter.core.spoon.check.MagicStringCheck;
import de.firemage.codelinter.core.spoon.check.MethodFlowComplexityCheck;
import de.firemage.codelinter.core.spoon.check.ObjectTypeCheck;
import de.firemage.codelinter.core.spoon.check.TooManySubclassesCheck;
import de.firemage.codelinter.core.spoon.check.UninstantiatedClassCheck;
import de.firemage.codelinter.core.spoon.check.UnusedVariableCheck;
import de.firemage.codelinter.core.spoon.check.VarProcessor;
import de.firemage.codelinter.core.spoon.check.VisibilityCheck;
import de.firemage.codelinter.core.spoon.check.reflect.ReflectImportCheck;
import de.firemage.codelinter.core.spoon.check.reflect.ReflectMethodCheck;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class SpoonLinter {
    public List<Problem> lint(UploadedFile file, JavaVersion javaVersion, File jar) throws CompilationException, IOException {
        // Use a custom class loader so because spoon won't class its standard URLClassLoader and will leak the handle to the jar file
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {jar.toURI().toURL()}, Thread.currentThread().getContextClassLoader())) {
            Launcher launcher = new Launcher();
            launcher.addInputResource(file.getSpoonFile());
            launcher.getEnvironment().setShouldCompile(false);
            launcher.getEnvironment().setSourceClasspath(new String[] {jar.getAbsolutePath()});
            launcher.getEnvironment().setNoClasspath(false);
            launcher.getEnvironment().setCommentEnabled(true);
            launcher.getEnvironment().setComplianceLevel(javaVersion.getVersionNumber());
            launcher.getEnvironment().setInputClassLoader(classLoader);

            CtModel model;
            try {
                model = launcher.buildModel();
            } catch (ModelBuildingException e) {
                throw new CompilationException("Failed to parse the code", e);
            }
            Factory factory = launcher.getFactory();

            ProblemLogger logger = new ProblemLogger(file.getFile());

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

            Check emptyAbstractClassCheck = new EmptyAbstractClassCheck(logger);
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

            Check tooManySubclassCheck = new TooManySubclassesCheck(logger);
            tooManySubclassCheck.check(model, factory);

            Check visibilityCheck = new VisibilityCheck(logger);
            visibilityCheck.check(model, factory);

            Check magicStringCheck = new MagicStringCheck(logger);
            magicStringCheck.check(model, factory);

            return logger.getProblems();
        }

    }
}
