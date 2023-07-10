package de.firemage.autograder.core.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class SeparateBinaryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final File binaryRootDirectory;
    private final Charset charset;

    protected SeparateBinaryFileManager(StandardJavaFileManager fileManager, File binaryRootDirectory, Charset charset) {
        super(fileManager);
        this.binaryRootDirectory = binaryRootDirectory;
        this.charset = charset;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new PhysicalFileObject(new File(binaryRootDirectory, createFileNameFromClass(className)), this.charset, null);
    }

    private String createFileNameFromClass(String className) {
        return className.replace(".", "/") + ".class";
    }
}
