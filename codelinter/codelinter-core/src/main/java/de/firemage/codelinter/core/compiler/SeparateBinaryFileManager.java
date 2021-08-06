package de.firemage.codelinter.core.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;

public class SeparateBinaryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final File binaryRootDirectory;

    protected SeparateBinaryFileManager(StandardJavaFileManager fileManager, File binaryRootDirectory) {
        super(fileManager);
        this.binaryRootDirectory = binaryRootDirectory;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new PhysicalFileObject(new File(binaryRootDirectory, createFileNameFromClass(className)));
    }

    private String createFileNameFromClass(String className) {
        return className.replace(".", "/") + ".class";
    }
}
