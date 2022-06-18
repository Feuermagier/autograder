package de.firemage.codelinter.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassInstrumentationVisitor extends ClassVisitor {
    private String name;
    public ClassInstrumentationVisitor(ClassVisitor visitor) {
        super(Opcodes.ASM9, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodInstrumentationVisitor(visitor, access, name, descriptor, this.name);
    }
}
