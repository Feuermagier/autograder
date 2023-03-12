package de.firemage.autograder.core.file;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.SourceInfo;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.CompilationResult;
import de.firemage.autograder.core.compiler.Compiler;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.integrated.ModelBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public class UploadedFile implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(UploadedFile.class);

    private final CodeModel model;
    private final SourceInfo source;
    private final CompilationResult compilationResult;

    private UploadedFile(CodeModel model, SourceInfo source, CompilationResult compilationResult) {
        this.model = model;
        this.source = source;
        this.compilationResult = compilationResult;
    }

    public static UploadedFile build(Path file, JavaVersion version, Path tmpLocation,
                                     Consumer<LinterStatus> statusConsumer)
        throws IOException,
        ModelBuildException, CompilationFailureException {
        var source = new SourceInfo(file, version);

        statusConsumer.accept(LinterStatus.COMPILING);
        Optional<CompilationResult> compilationResult = Compiler.compileToJar(source, tmpLocation, source.getVersion());
        if (compilationResult.isEmpty()) {
            return null;
        }

        statusConsumer.accept(LinterStatus.BUILDING_CODE_MODEL);
        var model = CodeModel.buildFor(source, compilationResult.get().jar());

        return new UploadedFile(model, source, compilationResult.get());
    }

    public SourceInfo getSource() {
        return this.source;
    }

    public CompilationResult getCompilationResult() {
        return compilationResult;
    }

    public CodeModel getModel() {
        return model;
    }

    @Override
    public void close() throws Exception {
        this.model.close();
        this.compilationResult.jar().toFile().delete();
    }
}
