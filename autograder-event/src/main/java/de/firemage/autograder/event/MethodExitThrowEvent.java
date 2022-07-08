package de.firemage.autograder.event;

import java.util.List;

public class MethodExitThrowEvent implements MethodEvent {
    private final String clazz;
    private final String methodName;
    private final String methodDesc;
    private final String exception;

    public MethodExitThrowEvent(String clazz, String methodName, String methodDesc, String exception) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.exception = exception;
    }

    @Override
    public String format() {
        return String.join(":",
            List.of("Throw", this.clazz, this.methodName, this.methodDesc, this.exception));
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

    public String getException() {
        return exception;
    }
}

