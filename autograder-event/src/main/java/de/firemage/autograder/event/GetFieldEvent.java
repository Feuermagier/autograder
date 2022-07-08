package de.firemage.autograder.event;

import java.util.List;

public class GetFieldEvent implements MethodEvent {
    private final String clazz;
    private final String methodName;
    private final String methodDesc;
    private final String target;
    private final String field;
    private final String value;

    public GetFieldEvent(String clazz, String methodName, String methodDesc, String target, String field,
                         String value) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.target = target;
        this.field = field;
        this.value = value;
    }

    @Override
    public String format() {
        return String.join(":",
            List.of("GetField", this.clazz, this.methodName, this.methodDesc, this.target, this.field, this.value));
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

    public String getTarget() {
        return target;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
