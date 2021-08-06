package de.firemage.codelinter.linter.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class PhysicalFileObject extends SimpleJavaFileObject {

    private final File file;

    public PhysicalFileObject(File file) {
        super(file.toURI(), Kind.SOURCE);
        this.file = file;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return Files.readString(this.file.toPath());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        this.file.getParentFile().mkdirs();
        this.file.createNewFile();

        return new FileOutputStream(this.file);
    }
}
