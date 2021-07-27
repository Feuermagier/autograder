package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.spoon.check.AssertProcessor;
import de.firemage.codelinter.linter.spoon.check.CatchProcessor;
import de.firemage.codelinter.linter.spoon.check.Check;
import de.firemage.codelinter.linter.spoon.check.IllegalExitProcessor;
import de.firemage.codelinter.linter.spoon.check.VarProcessor;
import de.firemage.codelinter.linter.spoon.check.reflect.ReflectImportCheck;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;
import spoon.support.compiler.SpoonProgress;

import java.util.List;

public class SpoonLinter {
    public List<Problem> lint(UploadedFile file, int javaLevel) throws CompilationException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(file.getSpoonFile());
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setComplianceLevel(javaLevel);

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

        return logger.getProblems();
    }
}
