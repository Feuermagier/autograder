package de.firemage.autograder.core.file;

import de.firemage.autograder.api.AbstractTempLocation;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.CompilationResult;
import de.firemage.autograder.core.compiler.Compiler;
import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.core.integrated.ModelBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class UploadedFile implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(UploadedFile.class);

    private final CodeModel model;
    private final SourceInfo source;
    private final CompilationResult compilationResult;
    private final ClassLoader classLoader;
    private final AbstractTempLocation tempLocation;

    private UploadedFile(CodeModel model, SourceInfo source, CompilationResult compilationResult, ClassLoader classLoader, AbstractTempLocation tempLocation) {
        this.model = model;
        this.source = source;
        this.compilationResult = compilationResult;
        this.classLoader = classLoader;
        this.tempLocation = tempLocation;
    }

    public UploadedFile copy() {
        try {
            return UploadedFile.build(this.source, this.tempLocation.createTempDirectory("copy"), unused -> {}, this.classLoader);
        } catch (IOException | CompilationFailureException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static UploadedFile build(
        Path file,
        JavaVersion version,
        AbstractTempLocation tmpLocation,
        Consumer<Translatable> statusConsumer,
        ClassLoader classLoader
    ) throws IOException, ModelBuildException, CompilationFailureException {
        return UploadedFile.build(new FileSourceInfo(file, version), tmpLocation, statusConsumer, classLoader);
    }

    public static UploadedFile build(
        SourceInfo source,
        AbstractTempLocation tmpLocation,
        Consumer<Translatable> statusConsumer,
        ClassLoader classLoader
    ) throws IOException, CompilationFailureException {
        Compiler compiler = new Compiler(tmpLocation, source.getVersion());
        statusConsumer.accept(LinterStatus.COMPILING.getMessage());
        Optional<CompilationResult> compilationResult = compiler.compileToJar(source);
        if (compilationResult.isEmpty()) {
            return null;
        }

        var model = CodeModel.buildFor(source, compilationResult.get().jar(), classLoader);

        return new UploadedFile(model, source, compilationResult.get(), classLoader, tmpLocation);
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
    public void close() throws IOException {
        this.model.close();
        this.compilationResult.jar().toFile().delete();
    }
}
