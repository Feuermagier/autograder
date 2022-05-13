package de.firemage.codelinter.core.dynamic;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassInstrumentationVisitor extends ClassVisitor {
    public ClassInstrumentationVisitor(ClassVisitor visitor) {
        super(Opcodes.ASM9, visitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (Type.getReturnType(descriptor).getSort() == Type.OBJECT) {
            return new MethodInstrumentationVisitor(visitor, access, name, descriptor);
        } else {
            return visitor;
        }
    }
}
