package de.firemage.codelinter.agent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class MethodInstrumentationVisitor extends AdviceAdapter {
    private final String className;

    public MethodInstrumentationVisitor(MethodVisitor visitor, int access, String name, String descriptor, String className) {
        super(Opcodes.ASM9, visitor, access, name, descriptor);
        this.className = className;
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == Opcodes.ARETURN) {
            dup();
            int returnValue = newLocal(this.getReturnType());
            storeLocal(returnValue);
            visitLdcInsn(this.className);
            visitLdcInsn(this.getName());
            visitLdcInsn(this.methodDesc);
            loadLocal(returnValue);
            invokeStatic(Type.getType("Lde/firemage/codelinter/agent/EventRecorder;"),
                new Method("recordReferenceReturn",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
        }
    }
}
