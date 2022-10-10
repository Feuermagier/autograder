package de.firemage.autograder.core.integrated;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

public final class SpoonStreamUtil {
    private static final Set<String> STREAM_CLASSES = Set.of("java.util.stream.Stream", "java.util.stream.IntStream", "java.util.stream.LongStream", "java.util.stream.DoubleStream");
    
    private SpoonStreamUtil() {}

    public static boolean isStream(CtTypeReference<?> type) {
        return STREAM_CLASSES.contains(type.getQualifiedName());
    }
    
    public static boolean isStreamOperation(CtInvocation<?> invocation) {
        return isStream(invocation.getType());
    }
    
    public static boolean isCollectOperation(CtInvocation<?> invocation, String name) {
        return isStreamOperation(invocation)
            && invocation.getExecutable().getSimpleName().equals("collect")
            && !invocation.getArguments().isEmpty()
            && invocation.getArguments().get(0) instanceof CtInvocation<?> collect
            && collect.getExecutable().getSimpleName().equals(name)
            && collect.getTarget() instanceof CtTypeAccess<?> type
            && type.getAccessedType().getQualifiedName().equals("java.util.streamCollectors");
    }
}
