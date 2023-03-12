package de.firemage.autograder.core;

import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.ModelBuildException;
import de.firemage.autograder.core.integrated.SpoonUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public class CodeModel implements AutoCloseable {
    private final URLClassLoader classLoader;
    private final Factory factory;
    private final CtModel model;
    private CtPackage basePackage;

    private CodeModel(SourceInfo file, Path jar) throws
        ModelBuildException, IOException {

        // Use a custom class loader because spoon won't close its standard URLClassLoader and will leak the handle to the jar file
        this.classLoader =
            new URLClassLoader(new URL[] {jar.toUri().toURL()}, Thread.currentThread().getContextClassLoader());

        Launcher launcher = new Launcher();
        launcher.addInputResource(file.getSpoonFile());
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setSourceClasspath(new String[] {jar.toAbsolutePath().toString()});
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setComplianceLevel(file.getVersion().getVersionNumber());
        launcher.getEnvironment().setInputClassLoader(classLoader);
        launcher.getEnvironment().setEncoding(file.getCharset());

        try {
            this.model = launcher.buildModel();
        } catch (ModelBuildingException e) {
            throw new ModelBuildException("Failed to parse the code", e);
        }
        this.factory = launcher.getFactory();
        
        // Find the base package
        this.model.processWith(new AbstractProcessor<CtType<?>>() {
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
    }

    public static CodeModel buildFor(SourceInfo file, Path jar) throws
        ModelBuildException, IOException {
        return new CodeModel(file, jar);
    }

    public Factory getFactory() {
        return factory;
    }

    public CtModel getModel() {
        return model;
    }

    public <E extends CtElement> void processWith(Processor<E> processor) {
        this.model.processWith(processor);
    }

    public CtClass<?> findClassByName(String name) {
        CtClass<?> clazz = this.model.filterChildren(
            child -> child instanceof CtClass<?> c && c.getQualifiedName().equals(name)).first();
        if (clazz == null) {
            throw new IllegalArgumentException("No class with name '" + name + "' found");
        }
        return clazz;
    }

    public CtMethod<?> findMethodBySignature(CtClass<?> clazz, String signature) {
        CtMethod<?> result = this.model.filterChildren(
            child -> child instanceof CtMethod<?> method && method.getSignature().equals(signature)).first();
        if (result == null) {
            throw new IllegalArgumentException(
                "No method in class " + clazz.getQualifiedName() + " with signature '" + signature + "' found");
        }
        return result;
    }

    public CtMethod<Void> findMain() {
        return this.model.filterChildren(child -> child instanceof CtMethod<?> method && SpoonUtil.isMainMethod(method))
            .map(c -> (CtMethod<Void>) c)
            .first();
    }

    public List<String> getAllPackageNames() {
        return this.model.filterChildren(c -> c instanceof CtPackage).map(p -> ((CtPackage) p).getQualifiedName())
            .list();
    }

    public CtPackage getBasePackage() {
        return basePackage;
    }

    @Override
    public void close() throws IOException {
        this.classLoader.close();
    }
}
