package de.firemage.autograder.core.integrated;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class MethodHierarchy {

    private final IdentityHashMap<CtMethod<?>, SurroundingMethods> methodHierarchy;

    public MethodHierarchy(CtModel model) {
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

    public Set<MethodOrLambda<?>> getDirectSuperMethods(CtMethod<?> method) {
        var surroundingMethods = this.methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Set.of();
        } else {
            return Collections.unmodifiableSet(surroundingMethods.superMethods);
        }
    }

    public Set<MethodOrLambda<?>> getDirectOverridingMethods(CtMethod<?> method) {
        var surroundingMethods = this.methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Set.of();
        } else {
            return Collections.unmodifiableSet(surroundingMethods.overridingMethods);
        }
    }

    public Stream<MethodOrLambda<?>> getAllOverridingMethods(CtMethod<?> method) {
        var surroundingMethods = this.methodHierarchy.get(method);
        if (surroundingMethods == null) {
            return Stream.of();
        } else {
            return surroundingMethods.overridingMethods
                    .stream()
                    .flatMap(m -> Stream.concat(Stream.of(m), this.getAllOverridingMethods(m.getMethod())));
        }
    }

    private void searchSuperTypesForSuperMethod(CtType<?> currentType, CtMethod<?> subMethod, Set<CtType<?>> visitedTypes) {
        if (currentType == null || visitedTypes.contains(currentType)) {
            return;
        }
        visitedTypes.add(currentType);

        if (currentType.getSuperclass() != null) {
            this.searchSuperMethodInType(currentType.getSuperclass().getTypeDeclaration(), subMethod, visitedTypes);
        }

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
