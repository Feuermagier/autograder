package de.firemage.autograder.core.file;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.compiler.CompilationFailureException;
import de.firemage.autograder.core.compiler.CompilationResult;
import de.firemage.autograder.core.compiler.Compiler;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.errorprone.TempLocation;
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

    public static UploadedFile build(
        Path file,
        JavaVersion version,
        TempLocation tmpLocation,
        Consumer<? super LinterStatus> statusConsumer,
        ClassLoader classLoader
    ) throws IOException, ModelBuildException, CompilationFailureException {
        return UploadedFile.build(new FileSourceInfo(file, version), tmpLocation, statusConsumer, classLoader);
    }

    public static UploadedFile build(
        SourceInfo source,
        TempLocation tmpLocation,
        Consumer<? super LinterStatus> statusConsumer,
        ClassLoader classLoader
    ) throws IOException, CompilationFailureException {
        Compiler compiler = new Compiler(tmpLocation, source.getVersion());
        statusConsumer.accept(LinterStatus.COMPILING);
        Optional<CompilationResult> compilationResult = compiler.compileToJar(source);
        if (compilationResult.isEmpty()) {
            return null;
        }

        var model = CodeModel.buildFor(source, compilationResult.get().jar(), classLoader);

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
    public void close() throws IOException {
        this.model.close();
        this.compilationResult.jar().toFile().delete();
    }
}
