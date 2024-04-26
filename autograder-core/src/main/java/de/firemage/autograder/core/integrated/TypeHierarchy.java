package de.firemage.autograder.core.integrated;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

public class TypeHierarchy {

    private final IdentityHashMap<CtType<?>, List<CtType<?>>> subTypes;

    public TypeHierarchy(CtModel model) {
        this.subTypes = new IdentityHashMap<>();

        for (CtType<?> type : model.getAllTypes()) {
            if (type.getSuperclass() != null) {
                this.recordSubtypeRelationship(type, type.getSuperclass().getTypeDeclaration());
            }
            type.getSuperInterfaces().forEach(superInterface -> this.recordSubtypeRelationship(type, superInterface.getTypeDeclaration()));
        }
    }

    public List<CtType<?>> getDirectSubTypes(CtType<?> type) {
        return Collections.unmodifiableList(this.subTypes.getOrDefault(type, List.of()));
    }

    private void recordSubtypeRelationship(CtType<?> subType, CtType<?> superType) {
        if (superType != null) {
            this.subTypes.computeIfAbsent(superType, t -> new ArrayList<>()).add(subType);
        }
    }
}
