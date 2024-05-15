package de.firemage.autograder.event;

public interface MethodEvent extends Event {
    String getOwningClass();
    String getMethodName();
    String getMethodDescriptor();
}
