package de.firemage.codelinter.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain domain, byte[] buffer) {
        // Don't modify JDK classes
        if (className.startsWith("java/") || className.startsWith("sun/") || className.startsWith("jdk/") || className.startsWith("de/firemage/codelinter/")) {
            return buffer;
        }
        System.out.println("AGENT: Modifying class " + className);

        ClassReader reader = new ClassReader(buffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassInstrumentationVisitor classVisitor = new ClassInstrumentationVisitor(writer);
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}
