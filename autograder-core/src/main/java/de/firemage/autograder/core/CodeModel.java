package de.firemage.autograder.core;

import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.integrated.ModelBuildException;
import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.processing.AbstractProcessor;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.NamedElementFilter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The model is build lazily to work better with the multithreaded core architecture.
 */
public final class CodeModel implements AutoCloseable {
    private final SourceInfo file;
    private final Path jar;
    private final ClassLoader userClassLoader;
    private final URLClassLoader classLoader;
    private Factory factory;
    private CtModel model;
    private CtPackage basePackage;
    private Optional<CtMethod<Void>> mainMethod;

    private CodeModel(SourceInfo file, Path jar, ClassLoader classLoader) {
        this.file = file;
        this.jar = jar;

        if (classLoader != null) {
            this.userClassLoader = classLoader;
            this.classLoader = null;
        } else {
            // Use a custom class loader because spoon won't close its standard URLClassLoader and will leak the handle to the jar file
            try {
                this.classLoader =
                        new URLClassLoader(new URL[]{jar.toUri().toURL()}, Thread.currentThread().getContextClassLoader());
                this.userClassLoader = null;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static CodeModel buildFor(SourceInfo file, Path jar, ClassLoader classLoader) {
        return new CodeModel(file, jar, classLoader);
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

    @SuppressWarnings("unchecked")
    public CtMethod<Void> findMain() {
        this.buildModelMaybe();

        // NOTE: this is intentional, so that the main method is only searched once
        if (this.mainMethod == null) {
            this.mainMethod = this.getModel()
                .getElements(new NamedElementFilter<>(CtMethod.class, "main"))
                .stream()
                .filter(SpoonUtil::isMainMethod)
                .findFirst()
                .map(ctMethod -> (CtMethod<Void>) ctMethod);
        }

        return this.mainMethod.orElse(null);
    }

    /**
     * Checks if the code has a main method.
     *
     * @return true if it has, false otherwise
     */
    public boolean hasMainMethod() {
        return this.findMain() != null;
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

            Launcher launcher = new Launcher();
            launcher.addInputResource(file.getSpoonResource());
            launcher.getEnvironment().setShouldCompile(false);
            launcher.getEnvironment().setSourceClasspath(new String[]{jar.toAbsolutePath().toString()});
            launcher.getEnvironment().setNoClasspath(false);
            launcher.getEnvironment().setCommentEnabled(true);
            launcher.getEnvironment().setComplianceLevel(this.file.getVersion().getVersionNumber());
            // The encoding might differ by file
            launcher.getEnvironment().setEncodingProvider(
                (spoonFile, fileBytes) -> this.file.getCompilationUnit(Path.of(spoonFile.getPath())).charset()
            );

            if (this.userClassLoader != null) {
                launcher.getEnvironment().setInputClassLoader(this.userClassLoader);
            } else {
                launcher.getEnvironment().setInputClassLoader(this.classLoader);
            }

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
