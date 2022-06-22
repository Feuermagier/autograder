package de.firemage.codelinter.agent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class MethodInstrumentationVisitor extends AdviceAdapter {
    private final String className;

    public MethodInstrumentationVisitor(MethodVisitor visitor, int access, String name, String descriptor,
                                        String className) {
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

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (opcode == Opcodes.PUTFIELD) {
            int value = newLocal(Type.getType(descriptor));
            storeLocal(value);
            int target = newLocal(Type.getObjectType(owner));
            storeLocal(target);
            visitLdcInsn(this.className);
            visitLdcInsn(this.getName());
            visitLdcInsn(this.methodDesc);
            loadLocal(target);
            visitLdcInsn(name);
            loadLocal(value);
            invokeStatic(Type.getType("Lde/firemage/codelinter/agent/EventRecorder;"),
                new Method("recordPutField",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;" + generifyDescriptor(descriptor) + ")V"));
            loadLocal(target);
            loadLocal(value);
        } else if (opcode == Opcodes.GETFIELD) {
            int target = newLocal(Type.getObjectType(owner));
            storeLocal(target);
            visitLdcInsn(this.className);
            visitLdcInsn(this.getName());
            visitLdcInsn(this.methodDesc);
            loadLocal(target);
            visitLdcInsn(name);
            loadLocal(target);
            getField(Type.getObjectType(owner), name, Type.getType(descriptor));
            invokeStatic(Type.getType("Lde/firemage/codelinter/agent/EventRecorder;"),
                new Method("recordGetField",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;" + generifyDescriptor(descriptor) + ")V"));
            loadLocal(target);
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }
    
    private String generifyDescriptor(String descriptor) {
        if (descriptor.startsWith("L") || descriptor.startsWith("[")) {
            return "Ljava/lang/Object;";
        } else {
            return descriptor;
        }
    }
}
