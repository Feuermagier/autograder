package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.file.UploadedFile;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class StaticAnalysis implements AutoCloseable {
    private final SpoonModel spoonModel;

    public StaticAnalysis(UploadedFile file, Path jar, Consumer<String> statusConsumer) throws ModelBuildException, IOException {
        this.spoonModel = new SpoonModel(file, jar, statusConsumer);

    }

    public SpoonModel getSpoonModel() {
        return this.spoonModel;
    }

    @Override
    public void close() throws IOException {
        this.spoonModel.close();
    }


}
