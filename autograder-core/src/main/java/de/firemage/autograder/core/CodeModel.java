package de.firemage.autograder.core;

import de.firemage.autograder.core.integrated.ModelBuildException;
import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.processing.AbstractProcessor;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * The model is build lazily to work better with the multithreaded core architecture.
 */
public class CodeModel implements AutoCloseable {
    private final SourceInfo file;
    private final Path jar;
    private URLClassLoader classLoader;
    private Factory factory;
    private CtModel model;
    private CtPackage basePackage;

    private CodeModel(SourceInfo file, Path jar) {
        this.file = file;
        this.jar = jar;
    }

    public static CodeModel buildFor(SourceInfo file, Path jar) {
        return new CodeModel(file, jar);
    }

    public void ensureModelBuild() {
        this.buildModelMaybe();
    }

    public Factory getFactory() {
        this.buildModelMaybe();
        return factory;
    }

    public CtModel getModel() {
        this.buildModelMaybe();
        return model;
    }

    public <E extends CtElement> void processWith(Processor<E> processor) {
        this.buildModelMaybe();
        this.model.processWith(processor);
    }

    public CtClass<?> findClassByName(String name) {
        this.buildModelMaybe();
        CtClass<?> clazz = this.model.filterChildren(
                child -> child instanceof CtClass<?> c && c.getQualifiedName().equals(name)).first();
        if (clazz == null) {
            throw new IllegalArgumentException("No class with name '" + name + "' found");
        }
        return clazz;
    }

    public CtMethod<?> findMethodBySignature(CtClass<?> clazz, String signature) {
        this.buildModelMaybe();
        CtMethod<?> result = this.model.filterChildren(
                child -> child instanceof CtMethod<?> method && method.getSignature().equals(signature)).first();
        if (result == null) {
            throw new IllegalArgumentException(
                    "No method in class " + clazz.getQualifiedName() + " with signature '" + signature + "' found");
        }
        return result;
    }

    public CtMethod<Void> findMain() {
        this.buildModelMaybe();
        return this.model.filterChildren(child -> child instanceof CtMethod<?> method && SpoonUtil.isMainMethod(method))
                .map(c -> (CtMethod<Void>) c)
                .first();
    }

    public List<String> getAllPackageNames() {
        this.buildModelMaybe();
        return this.model.filterChildren(c -> c instanceof CtPackage).map(p -> ((CtPackage) p).getQualifiedName())
                .list();
    }

    public CtPackage getBasePackage() {
        this.buildModelMaybe();
        return basePackage;
    }

    @Override
    public void close() throws IOException {
        if (this.classLoader != null) {
            this.classLoader.close();
        }
    }

    private void buildModelMaybe() {
        // First check without synchronization
        if (this.model != null) {
            return;
        }

        synchronized (this) {
            // Check again that the model hasn't been build before entering the synchronized block
            if (this.model != null) {
                return;
            }

            // Use a custom class loader because spoon won't close its standard URLClassLoader and will leak the handle to the jar file
            try {
                this.classLoader =
                        new URLClassLoader(new URL[]{jar.toUri().toURL()}, Thread.currentThread().getContextClassLoader());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            Launcher launcher = new Launcher();
            launcher.addInputResource(file.getSpoonResource());
            launcher.getEnvironment().setShouldCompile(false);
            launcher.getEnvironment().setSourceClasspath(new String[]{jar.toAbsolutePath().toString()});
            launcher.getEnvironment().setNoClasspath(false);
            launcher.getEnvironment().setCommentEnabled(true);
            launcher.getEnvironment().setComplianceLevel(file.getVersion().getVersionNumber());
            launcher.getEnvironment().setInputClassLoader(classLoader);
            launcher.getEnvironment().setEncoding(file.getCharset());

            CtModel model;
            try {
                model = launcher.buildModel();
            } catch (ModelBuildingException e) {
                throw new RuntimeException(new ModelBuildException("Failed to parse the code", e));
            }
            this.factory = launcher.getFactory();

            // Find the base package
            model.processWith(new AbstractProcessor<CtType<?>>() {
                @Override
                public void process(CtType<?> type) {
                    if (type.getPackage() == null || type.getPackage().getQualifiedName().startsWith("java.")) {
                        return;
                    }

                    if (basePackage == null) {
                        basePackage = type.getPackage();
                        return;
                    }

                    var typePackage = type.getPackage().getQualifiedName();
                    while (!typePackage.startsWith(basePackage.getQualifiedName())) {
                        basePackage = basePackage.getDeclaringPackage();
                    }
                }
            });

            // Only set the model at the end when everything has been initialized
            this.model = model;
        }
    }
}
