package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.core.file.UploadedFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DynamicAnalysis {
    private final JarFile jarFile;

    public DynamicAnalysis(Path jar) throws IOException {
        this.jarFile = new JarFile(jar.toFile());
    }

    public void run() throws IOException {
        var iterator = this.jarFile.entries().asIterator();
        while (iterator.hasNext()) {
            instrumentFile(iterator.next());
        }
    }

    private void instrumentFile(JarEntry jarEntry) throws IOException {
        ClassReader reader = new ClassReader(this.jarFile.getInputStream(jarEntry));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassInstrumentationVisitor classVisitor = new ClassInstrumentationVisitor(writer);
        reader.accept(new CheckClassAdapter(classVisitor), ClassReader.EXPAND_FRAMES);
        try (OutputStream out = Files.newOutputStream(Path.of("Test/Test.class"))) {
            out.write(writer.toByteArray());
        }
    }
}
