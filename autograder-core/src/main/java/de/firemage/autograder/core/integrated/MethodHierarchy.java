package de.firemage.autograder.core.integrated;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides a hierarchy of overwritten methods within the code model.
 * The hierarchy is built once at construction time for all methods, so that all subsequent queries are fast.
 */
public class MethodHierarchy {
    private static final String METADATA_KEY = "autograder_method_hierarchy";

    private final IdentityHashMap<CtMethod<?>, SurroundingMethods> methodHierarchy;

    private MethodHierarchy(CtModel model) {
        this.methodHierarchy = new IdentityHashMap<>();

        // Using a scanner instead of queries/filters so that we traverse the model only once
        model.getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtMethod(CtMethod<T> method) {
                if (!method.isStatic() && !method.isPrivate()) {
                    methodHierarchy.computeIfAbsent(method, m -> SurroundingMethods.empty());
                    searchSuperTypesForSuperMethod(method.getDeclaringType(), method, Collections.newSetFromMap(new IdentityHashMap<>()));
                }
                super.visitCtMethod(method);
            }

            @Override
            public <T> void visitCtLambda(CtLambda<T> lambda) {
                var overriddenMethod = lambda.getOverriddenMethod();
                if (overriddenMethod != null) {
                    methodHierarchy.computeIfAbsent(overriddenMethod, m -> SurroundingMethods.empty()).overridingMethods.add(new MethodOrLambda<>(lambda));
                }
                super.visitCtLambda(lambda);
            }
        });
    }

    public static void buildFor(CtModel model) {
        MethodHierarchy methodHierarchy = new MethodHierarchy(model);
        model.getRootPackage().putMetadata(METADATA_KEY, methodHierarchy);
    }

    public static MethodHierarchy getFor(CtElement element) {
        var methodHierarchy = (MethodHierarchy) element.getFactory().getModel().getRootPackage().getMetadata(METADATA_KEY);
        if (methodHierarchy == null) {
            throw new IllegalStateException("MethodHierarchy not built for this model");
        }
        return methodHierarchy;
    }

    /**
     * Finds all methods that are *direct* super methods of the given method.
     * In the following example, when calling {@code getDirectSuperMethods(C.m)}, only {@code B.m} is returned:
     * <pre>
     *     class A { void m() {} }
     *     class B extends A { void m() {} }
     *     class C extends B { void m() {} }
     * </pre>
     *
     * @param method
     * @return
     */
    public static Set<MethodOrLambda<?>> getDirectSuperMethods(CtMethod<?> method) {
        var surroundingMethods = MethodHierarchy.getFor(method).methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Set.of();
        } else {
            return Collections.unmodifiableSet(surroundingMethods.superMethods);
        }
    }

    /**
     * Finds all methods that *directly* override the given method.
     * In the following example, when calling {@code getDirectOverridingMethods(A.m)}, only {@code B.m} is returned:
     * <pre>
     *     class A { void m() {} }
     *     class B extends A { void m() {} }
     *     class C extends B { void m() {} }
     * </pre>
     *
     * @param method
     * @return
     */
    public static Set<MethodOrLambda<?>> getDirectOverridingMethods(CtMethod<?> method) {
        var surroundingMethods = MethodHierarchy.getFor(method).methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Set.of();
        } else {
            return Collections.unmodifiableSet(surroundingMethods.overridingMethods);
        }
    }

    /**
     * Finds all methods overriding the given method, including overrides of methods overriding the given method.
     * In the following example, when calling {@code getDirectOverridingMethods(A.m)}, both {@code B.m} and {@code C.m}
     * are returned:
     * <pre>
     *     class A { void m() {} }
     *     class B extends A { void m() {} }
     *     class C extends B { void m() {} }
     * </pre>
     *
     * @param method
     * @return
     */
    public static Stream<MethodOrLambda<?>> streamAllOverridingMethods(CtMethod<?> method) {
        var surroundingMethods = MethodHierarchy.getFor(method).methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Stream.of();
        } else {
            return surroundingMethods.overridingMethods
                    .stream()
                    .flatMap(m -> Stream.concat(Stream.of(m), MethodHierarchy.streamAllOverridingMethods(m.getMethod())));
        }
    }

    /**
     * Checks if the method overrides any other method.
     * In the following example, this is true for B.m, but not for A.m.
     * <pre>
     *     class A { void m() {} }
     *     class B extends A { void m() {} }
     * </pre>
     *
     * @param method
     * @return
     */
    public static boolean isOverridingMethod(CtMethod<?> method) {
        var surroundingMethods = MethodHierarchy.getFor(method).methodHierarchy.get(method);
        return surroundingMethods != null && !surroundingMethods.superMethods.isEmpty();
    }

    /**
     * Checks if any methods overrides this method.
     * In the following example, this is true for A.m, but not for B.m.
     * <pre>
     *     class A { void m() {} }
     *     class B extends A { void m() {} }
     * </pre>
     *
     * @param method
     * @return
     */
    public static boolean isOverriddenMethod(CtMethod<?> method) {
        var surroundingMethods = MethodHierarchy.getFor(method).methodHierarchy.get(method);
        return surroundingMethods != null && !surroundingMethods.overridingMethods.isEmpty();
    }

    private void searchSuperTypesForSuperMethod(CtType<?> currentType, CtMethod<?> subMethod, Set<CtType<?>> visitedTypes) {
        if (currentType == null || visitedTypes.contains(currentType)) {
            return;
        }
        visitedTypes.add(currentType);

        var superType = currentType.getSuperclass();
        if (currentType.getSuperclass() == null) {
            // Finally visit Object
            superType = currentType.getFactory().Type().objectType();
        }
        this.searchSuperMethodInType(superType.getTypeDeclaration(), subMethod, visitedTypes);

        for (var superInterface : currentType.getSuperInterfaces()) {
            this.searchSuperMethodInType(superInterface.getTypeDeclaration(), subMethod, visitedTypes);
        }
    }

    private void searchSuperMethodInType(CtType<?> currentType, CtMethod<?> subMethod, Set<CtType<?>> visitedTypes) {
        for (CtMethod<?> method : currentType.getMethods()) {
            if (method.isStatic() || method.isPrivate()) {
                continue;
            }

            if (subMethod.isOverriding(method)) {
                this.methodHierarchy.computeIfAbsent(method, k -> SurroundingMethods.empty()).overridingMethods.add(new MethodOrLambda(subMethod));
                this.methodHierarchy.get(subMethod).superMethods.add(new MethodOrLambda(method)); // the entry is already present in the map

                // We only want direct super methods
                return;
            }
        }

        // No super method, continue search in parents
        this.searchSuperTypesForSuperMethod(currentType, subMethod, visitedTypes);
    }

    private record SurroundingMethods(Set<MethodOrLambda<?>> superMethods, Set<MethodOrLambda<?>> overridingMethods) {
        public static SurroundingMethods empty() {
            return new SurroundingMethods(new HashSet<>(), new HashSet<>());
        }
    }

    public static class MethodOrLambda<T> {
        private CtMethod<T> method;
        private CtLambda<T> lambda;

        public MethodOrLambda(CtMethod<T> method) {
            this.method = method;
        }

        public MethodOrLambda(CtLambda<T> lambda) {
            this.lambda = lambda;
        }

        public CtMethod<T> getMethod() {
            return this.method;
        }

        public CtLambda<T> getLambda() {
            return this.lambda;
        }

        public CtExecutable<T> getExecutable() {
            return this.method != null ? this.method : this.lambda;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodOrLambda<?> that = (MethodOrLambda<?>) o;
            return this.method == that.method && this.lambda == that.lambda;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(this.method), System.identityHashCode(this.lambda));
        }
    }
}
