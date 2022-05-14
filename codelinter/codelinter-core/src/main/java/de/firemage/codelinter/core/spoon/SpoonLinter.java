package de.firemage.codelinter.core.spoon;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.analysis.MethodAnalysis;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpoonLinter {
    public List<Problem> lint(UploadedFile file, Path jar, List<SpoonCheck> checks) throws CompilationException, IOException {
        // Use a custom class loader because spoon won't close its standard URLClassLoader and will leak the handle to the jar file
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, Thread.currentThread().getContextClassLoader())) {
            Launcher launcher = new Launcher();
            launcher.addInputResource(file.getSpoonFile());
            launcher.getEnvironment().setShouldCompile(false);
            launcher.getEnvironment().setSourceClasspath(new String[] {jar.toAbsolutePath().toString()});
            launcher.getEnvironment().setNoClasspath(false);
            launcher.getEnvironment().setCommentEnabled(true);
            launcher.getEnvironment().setComplianceLevel(file.getVersion().getVersionNumber());
            launcher.getEnvironment().setInputClassLoader(classLoader);

            CtModel model;
            try {
                model = launcher.buildModel();
            } catch (ModelBuildingException e) {
                throw new CompilationException("Failed to parse the code", e);
            }
            Factory factory = launcher.getFactory();

            MethodAnalysis methodAnalysis = new MethodAnalysis(model);
            methodAnalysis.run();
            List<Problem> problems = new ArrayList<>();
            for (SpoonCheck check : checks) {
                CodeProcessor processor = check.getProcessor().get();
                problems.addAll(processor.check(model, factory));
            }

            return problems;
        }

    }
}
