package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonCheck;
import de.firemage.codelinter.core.spoon.analysis.MethodAnalysis;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import de.firemage.codelinter.event.Event;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IntegratedAnalysis implements AutoCloseable {
    private final Path jar;
    private final URLClassLoader classLoader;
    private final Factory factory;
    private final CtModel model;
    private List<Event> events = new ArrayList<>();
    private Path tmpLocation;

    public IntegratedAnalysis(UploadedFile file, Path jar, Path tmpLocation) throws CompilationException, IOException {
        this.jar = jar;
        this.tmpLocation = tmpLocation;

        // Use a custom class loader because spoon won't close its standard URLClassLoader and will leak the handle to the jar file
        this.classLoader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, Thread.currentThread().getContextClassLoader());

        Launcher launcher = new Launcher();
        launcher.addInputResource(file.getSpoonFile());
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setSourceClasspath(new String[] {jar.toAbsolutePath().toString()});
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setComplianceLevel(file.getVersion().getVersionNumber());
        launcher.getEnvironment().setInputClassLoader(classLoader);

        try {
            this.model = launcher.buildModel();
        } catch (ModelBuildingException e) {
            throw new CompilationException("Failed to parse the code", e);
        }
        this.factory = launcher.getFactory();
    }

    public void runDynamicAnalysis() throws IOException, InterruptedException {
        String mainClass = this.findMain().getParent(CtClass.class).getQualifiedName().replace(".", "/");
        DynamicAnalysis analysis = new DynamicAnalysis(this.tmpLocation, this.jar, Path.of("codelinter-executor/target/codelinter-executor-1.0-SNAPSHOT.jar"), mainClass);
        this.events = analysis.run();
    }

    public CtMethod<Void> findMain() {
        return this.model.filterChildren(child -> child instanceof CtMethod<?> method && isMain(method))
                .map(c -> (CtMethod<Void>) c)
                .first();
    }

    private boolean isMain(CtMethod<?> method) {
        return method.getSimpleName().equals("main")
                && method.getType().getQualifiedName().equals("void")
                && method.getParameters().size() == 1
                && method.getParameters().get(0).getType().getQualifiedName().equals("java.lang.String[]");
    }

    public List<Problem> lint(List<SpoonCheck> checks) {
        //MethodAnalysis methodAnalysis = new MethodAnalysis(this.model);
        //methodAnalysis.run();

        List<Problem> problems = new ArrayList<>();
        for (SpoonCheck check : checks) {
            CodeProcessor processor = check.getProcessor().get();
            problems.addAll(processor.check(this.model, this.factory));
        }

        return problems;
    }

    @Override
    public void close() throws IOException {
        this.classLoader.close();
    }
}