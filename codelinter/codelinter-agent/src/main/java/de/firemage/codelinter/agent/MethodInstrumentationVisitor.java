package de.firemage.codelinter.agent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class MethodInstrumentationVisitor extends AdviceAdapter {
    private static final Type RECORDER = Type.getType("Lde/firemage/codelinter/agent/EventRecorder;");
    private final String className;
    private final Label startFinally;

    public MethodInstrumentationVisitor(MethodVisitor visitor, int access, String name, String descriptor,
                                        String className) {
        super(Opcodes.ASM9, visitor, access, name, descriptor);
        this.className = className;
        this.startFinally = new Label();
    }

    @Override
    protected void onMethodEnter() {
        visitLabel(this.startFinally);
        visitLdcInsn(this.className);
        visitLdcInsn(this.getName());
        visitLdcInsn(this.methodDesc);
        invokeStatic(RECORDER,
            new Method("recordMethodEnter",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
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
            invokeStatic(RECORDER,
                new Method("recordReferenceReturn",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
        } else if (opcode != Opcodes.ATHROW && opcode != Opcodes.RETURN) {
            dup();
            int returnValue = newLocal(this.getReturnType());
            storeLocal(returnValue);
            visitLdcInsn(this.className);
            visitLdcInsn(this.getName());
            visitLdcInsn(this.methodDesc);
            loadLocal(returnValue);
            box(this.getReturnType());
            invokeStatic(RECORDER,
                new Method("recordPrimitiveReturn",
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
            invokeStatic(RECORDER,
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
            invokeStatic(RECORDER,
                new Method("recordGetField",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;" + generifyDescriptor(descriptor) + ")V"));
            loadLocal(target);
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label endFinally = new Label();
        visitTryCatchBlock(this.startFinally, endFinally, endFinally, "java/lang/Throwable");
        visitLabel(endFinally);
        dup();
        int exception = newLocal(Type.getObjectType("Ljava/lang/Throwable;"));
        storeLocal(exception);
        visitLdcInsn(this.className);
        visitLdcInsn(this.getName());
        visitLdcInsn(this.methodDesc);
        loadLocal(exception);
        invokeStatic(RECORDER, new Method("recordExitThrow", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V"));
        throwException();
        super.visitMaxs(maxStack, maxLocals);
    }

    private String generifyDescriptor(String descriptor) {
        if (descriptor.startsWith("L") || descriptor.startsWith("[")) {
            return "Ljava/lang/Object;";
        } else {
            return descriptor;
        }
    }
}
