package de.firemage.autograder.core.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
        this.file.getParentFile().mkdirs();
        this.file.createNewFile();

        return new FileOutputStream(this.file);
    }
}
