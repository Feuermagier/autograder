package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.spoon.processor.AssertProcessor;
import de.firemage.codelinter.linter.spoon.processor.CatchProcessor;
import de.firemage.codelinter.linter.spoon.processor.IllegalExitProcessor;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.support.compiler.ZipFolder;

import java.io.File;
import java.util.List;

public class SpoonLinter {
    public List<Problem> lint(UploadedFile file, int javaLevel) throws CompilationException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(file.getSpoonFile());
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setComplianceLevel(javaLevel);

        CtModel model;
        try {
            model = launcher.buildModel();
        } catch (ModelBuildingException e) {
            throw new CompilationException("Failed to build a code model", e);
        }

        ProblemLogger logger = new ProblemLogger();

        CatchProcessor catchProcessor = new CatchProcessor(logger);
        model.processWith(catchProcessor);

        IllegalExitProcessor exitProcessor = new IllegalExitProcessor(logger);
        model.processWith(exitProcessor);

        AssertProcessor assertProcessor = new AssertProcessor(logger);
        model.processWith(assertProcessor);

        return logger.getProblems();
    }
}
