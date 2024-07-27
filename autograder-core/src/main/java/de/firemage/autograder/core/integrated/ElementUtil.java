package de.firemage.autograder.core.integrated;

import spoon.processing.FactoryAccessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class ElementUtil {
    private ElementUtil() {
    }


    public static boolean isAnyNestedOrSame(CtElement ctElement, Set<? extends CtElement> potentialParents) {
        // CtElement::hasParent will recursively call itself until it reaches the root
        // => inefficient and might cause a stack overflow

        if (potentialParents.contains(ctElement)) {
            return true;
        }

        for (CtElement parent : parents(ctElement)) {
            if (potentialParents.contains(parent)) {
                return true;
            }
        }

        return false;
    }

    public static CtPackage getRootPackage(FactoryAccessor element) {
        return element.getFactory().getModel().getRootPackage();
    }

    public static boolean isNestedOrSame(CtElement element, CtElement parent) {
        Set<CtElement> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.add(parent);

        return element == parent || ElementUtil.isAnyNestedOrSame(element, set);
    }

    /**
     * Returns an iterable over all parents of the given element.
     *
     * @param ctElement the element to get the parents of
     * @return an iterable over all parents, the given element is not included
     */
    public static Iterable<CtElement> parents(CtElement ctElement) {
        return () -> new Iterator<>() {
            private CtElement current = ctElement;

            @Override
            public boolean hasNext() {
                return this.current.isParentInitialized();
            }

            @Override
            public CtElement next() throws NoSuchElementException {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("No more parents");
                }

                CtElement result = this.current.getParent();
                this.current = result;
                return result;
            }
        };
    }

    public static <P extends CtElement> P getParentOrSelf(CtElement element, Class<P> parentType) {
        Objects.requireNonNull(element);
        if (parentType.isAssignableFrom(element.getClass())) {
            return (P) element;
        }
        return element.getParent(parentType);
    }

    public static int getParameterIndex(CtParameter<?> parameter, CtExecutable<?> executable) {
        for (int i = 0; i < executable.getParameters().size(); i++) {
            if (executable.getParameters().get(i) == parameter) {
                return i;
            }
        }
        throw new IllegalArgumentException("Parameter not found in executable");
    }

    public static Optional<CtJavaDoc> getJavadoc(CtElement element) {
        if (element.getComments().isEmpty() || !(element.getComments().get(0) instanceof CtJavaDoc)) {
            // TODO lookup inherited javadoc
            return Optional.empty();
        } else {
            return Optional.of(element.getComments().get(0).asJavaDoc());
        }
    }

    private record IdentityKey<T>(T value) {
        public static <T> IdentityKey<T> of(T value) {
            return new IdentityKey<>(value);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            IdentityKey<?> that = (IdentityKey<?>) obj;
            return this.value == that.value();
        }
    }

    private static <T, E> HashSet<T> newHashSet(Iterator<? extends E> elements, Function<E, ? extends T> mapper) {
        HashSet<T> set = new HashSet<>();
        for (; elements.hasNext(); set.add(mapper.apply(elements.next())));
        return set;
    }
    /**
     * Finds the closest common parent of the given elements.
     *
     * @param firstElement the first element to find the common parent of
     * @param others any amount of other elements to find the common parent of
     * @return the closest common parent of the given elements or the firstElement itself if others is empty
     */
    public static CtElement findCommonParent(CtElement firstElement, Iterable<? extends CtElement> others) {
        // CtElement::hasParent will recursively call itself until it reaches the root
        // => inefficient and might cause a stack overflow

        // add all parents of the firstElement to a set sorted by distance to the firstElement:
        Set<IdentityKey<CtElement>> ctParents = new LinkedHashSet<>();
        ctParents.add(IdentityKey.of(firstElement));
        parents(firstElement).forEach(element -> ctParents.add(IdentityKey.of(element)));

        for (CtElement other : others) {
            // only keep the parents that the firstElement and the other have in common
            ctParents.retainAll(newHashSet(parents(other).iterator(), IdentityKey::of));
        }

        // the first element in the set is the closest common parent
        return ctParents.iterator().next().value();
    }

    public static SourcePosition findPosition(CtElement ctElement) {
        if (ctElement.getPosition().isValidPosition()) {
            return ctElement.getPosition();
        }

        for (CtElement element : parents(ctElement)) {
            if (element.getPosition().isValidPosition()) {
                return element.getPosition();
            }
        }

        return null;
    }

    public static CtElement findValidPosition(CtElement ctElement) {
        CtElement result = ctElement;
        while (result != null && !result.getPosition().isValidPosition()) {
            result = result.getParent();
        }
        return result;
    }
}
