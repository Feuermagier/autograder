package de.firemage.codelinter.event;

public interface MethodEvent extends Event {
    String getOwningClass();
    String getMethodName();
    String getMethodDescriptor();
}
