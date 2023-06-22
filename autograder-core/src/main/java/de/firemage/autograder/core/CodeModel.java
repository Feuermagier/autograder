package de.firemage.autograder.core;

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
        return this.getModel()
            .getElements(new NamedElementFilter<>(CtMethod.class, "main"))
            .stream()
            .filter(SpoonUtil::isMainMethod)
            .findFirst().orElse(null);
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
            launcher.getEnvironment().setEncoding(file.getCharset());

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
            this.basePackage = SpoonUtil.findCommonPackage(model.getAllTypes());

            // Only set the model at the end when everything has been initialized
            this.model = model;
        }
    }
}
