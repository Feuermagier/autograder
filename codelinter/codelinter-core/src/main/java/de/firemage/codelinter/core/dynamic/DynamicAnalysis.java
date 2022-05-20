package de.firemage.codelinter.core.dynamic;

import de.firemage.codelinter.event.Event;
import de.firemage.codelinter.event.MethodEvent;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import java.util.List;
import java.util.stream.Stream;

public class DynamicAnalysis {

    private final List<TestRunResult> results;

    public DynamicAnalysis(List<TestRunResult> results) {
        this.results = results;
    }

    public List<TestRunResult> getResults() {
        return results;
    }

    public Stream<Event> getAllEvents() {
        return this.results.stream().flatMap(result -> result.events().stream());
    }

    public Stream<MethodEvent> findEventsForMethod(CtMethod<?> method) {
        String descriptor = createDescriptor(method);
        return this.getAllEvents()
                .filter(e -> e instanceof MethodEvent)
                .map(e -> (MethodEvent) e)
                .filter(e -> e.getOwningClass().equals(method.getDeclaringType().getQualifiedName().replace(".", "/")))
                .filter(e -> e.getMethodName().equals(method.getSimpleName()))
                .filter(e -> e.getMethodDescriptor().equals(descriptor));
    }

    private String createDescriptor(CtMethod<?> method) {
        StringBuilder descriptor = new StringBuilder("(");
        for (CtParameter<?> parameter : method.getParameters()) {
            descriptor.append(internalizeType(parameter.getType()));
        }
        descriptor.append(")");
        descriptor.append(internalizeType(method.getType()));
        return descriptor.toString();
    }

    private String internalizeType(CtTypeReference<?> type) {
        if (type.isArray()) {
            return "[" + internalizeType(((CtArrayTypeReference<?>) type).getComponentType());
        }
        return switch (type.getQualifiedName()) {
            case "byte" -> "B";
            case "char" -> "C";
            case "double" -> "D";
            case "float" -> "F";
            case "int" -> "I";
            case "long" -> "J";
            case "short" -> "S";
            case "boolean" -> "Z";
            case "void" -> "V";
            default -> "L" + type.getQualifiedName().replace(".", "/") + ";";
        };
    }
}
