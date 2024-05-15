package de.firemage.autograder.event;

import java.util.List;

public class MethodEnterEvent implements MethodEvent {
    private final String clazz;
    private final String methodName;
    private final String methodDesc;

    public MethodEnterEvent(String clazz, String methodName, String methodDesc) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    @Override
    public String format() {
        return String.join(":",
            List.of("Enter", this.clazz, this.methodName, this.methodDesc));
    }

    @Override
    public String getOwningClass() {
        return this.clazz;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public String getMethodDescriptor() {
        return this.methodDesc;
    }
}
