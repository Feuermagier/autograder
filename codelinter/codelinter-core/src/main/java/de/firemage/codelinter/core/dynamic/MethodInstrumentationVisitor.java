package de.firemage.codelinter.core.dynamic;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import java.io.PrintStream;

public class MethodInstrumentationVisitor extends AdviceAdapter {
    public MethodInstrumentationVisitor(MethodVisitor visitor, int access, String name, String descriptor) {
        super(Opcodes.ASM9, visitor, access, name, descriptor);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == Opcodes.ARETURN) {
            dup();
            getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));
            swap();
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/Object;)V"));
        }
    }
}
