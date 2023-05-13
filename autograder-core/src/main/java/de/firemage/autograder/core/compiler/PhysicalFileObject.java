package de.firemage.autograder.core.compiler;

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

public class PhysicalFileObject extends SimpleJavaFileObject {

    private final File file;
    private final Charset charset;

    public PhysicalFileObject(File file, Charset charset) {
        super(file.toURI(), Kind.SOURCE);
        this.file = file;
        this.charset = charset;
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
        return new OutputStreamWriter(this.openOutputStream(), this.charset);
    }
}
