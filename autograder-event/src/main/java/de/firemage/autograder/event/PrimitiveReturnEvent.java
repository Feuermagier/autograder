package de.firemage.autograder.event;

import java.util.List;

public class PrimitiveReturnEvent implements MethodEvent {
    private final String clazz;
    private final String methodName;
    private final String methodDesc;
    private final Object value;

    public PrimitiveReturnEvent(String clazz, String methodName, String methodDesc, Object value) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.value = value;
    }

    public Object getValue() {
        return this.value;
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

    @Override
    public String format() {
        return String.join(":",
            List.of("PrimRet", this.clazz, this.methodName, this.methodDesc, this.value.toString()));
    }
}
