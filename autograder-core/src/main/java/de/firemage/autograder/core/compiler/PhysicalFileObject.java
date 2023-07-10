package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.SerializableCharset;
import de.firemage.autograder.core.file.CompilationUnit;
import de.firemage.autograder.core.file.SourcePath;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class PhysicalFileObject implements CompilationUnit, JavaFileObject {
    private final File file;
    private final SourcePath path;
    private final SerializableCharset charset;

    public PhysicalFileObject(File file, Charset charset, SourcePath path) {
        this.file = file;
        this.path = path;
        this.charset = new SerializableCharset(charset);
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

    // This is mostly copy-pasted from SimpleJavaFileObject implementation
    @Override
    public URI toUri() {
        return this.file.toURI();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new StringReader(this.getCharContent(ignoreEncodingErrors).toString());
    }

    @Override
    public Writer openWriter() throws IOException {
        // ensure that the correct charset is used
        return new OutputStreamWriter(this.openOutputStream(), this.charset());
    }

    @Override
    public long getLastModified() {
        return 0L;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        String baseName = simpleName + kind.extension;
        return kind == this.getKind()
            && (baseName.equals(this.toUri().getPath())
            || this.toUri().getPath().endsWith("/" + baseName));
    }

    @Override
    public NestingKind getNestingKind() {
        return null;
    }

    @Override
    public Modifier getAccessLevel() {
        return null;
    }
}
