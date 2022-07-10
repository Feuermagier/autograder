package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.soot.SootModel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class StaticAnalysis implements AutoCloseable {
    private final SpoonModel spoonModel;
    private final SootModel sootModel;

    public StaticAnalysis(UploadedFile file, Path jar, Consumer<String> statusConsumer) throws ModelBuildException, IOException {
        this.spoonModel = new SpoonModel(file, jar, statusConsumer);
        this.sootModel = new SootModel(jar, this.spoonModel, statusConsumer);
    }

    public SpoonModel getSpoonModel() {
        return this.spoonModel;
    }

    @Override
    public void close() throws IOException {
        this.spoonModel.close();
    }


}
