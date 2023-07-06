package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.file.CompilationUnit;
import de.firemage.autograder.core.file.SourcePath;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class PhysicalFileObject extends SimpleJavaFileObject implements CompilationUnit {

    private final File file;
    private final SourcePath path;
    private final Charset charset;

    public PhysicalFileObject(File file, Charset charset, SourcePath path) {
        super(file.toURI(), Kind.SOURCE);
        this.file = file;
        this.path = path;
        this.charset = charset;
    }

    @Override
    public String getName() {
        // NOTE: PMD relies on this method to return something that looks like a path
        return this.path().toString();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return Files.readString(this.file.toPath(), this.charset);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        Files.createDirectories(this.file.getParentFile().toPath());
        try {
            Files.createFile(this.file.toPath());
        } catch (FileAlreadyExistsException ignored) {
            // ignored
        }

        return new FileOutputStream(this.file);
    }

    @Override
    public Writer openWriter() throws IOException {
        // ensure that the correct charset is used
        return new OutputStreamWriter(this.openOutputStream(), this.charset());
    }

    @Override
    public JavaFileObject toJavaFileObject() {
        return this;
    }

    @Override
    public Charset charset() {
        return this.charset;
    }

    @Override
    public SourcePath path() {
        return this.path;
    }
}
