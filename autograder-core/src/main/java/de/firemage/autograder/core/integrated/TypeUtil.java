package de.firemage.autograder.core.integrated;

import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility class for operations on types.
 */
public final class TypeUtil {
    private TypeUtil() {
    }

    public static boolean hasSubtype(CtType<?> ctType) {
        return UsesFinder.subtypesOf(ctType, false).hasAny();
    }

    /**
     * Returns a stream that iterates over all super types of the given type.
     *
     * @param ctType the type to get the super types of
     * @return a stream over all super types of the given type.
     */
    public static CtElementStream<CtType<?>> streamAllSuperTypes(CtTypeInformation ctType) {
        return CtElementStream.of(allSuperTypes(ctType));
    }

    /**
     * Returns an iterable that iterates over all super types of the given type.
     *
     * @param ctType the type to get the super types of
     * @return an iterable that can produce multiple iterator over all super types of the given type. The iterable does not yield duplicates.
     */
    public static Iterable<CtType<?>> allSuperTypes(CtTypeInformation ctType) {
        return () -> new Iterator<>() {
            private final Collection<CtTypeReference> visited = new HashSet<>();
            private final Deque<CtTypeReference> queue;

            // we can't override the constructor of an anonymous class, so we have to do this:
            {
                // first we queue the superclass (if any) and then all superinterfaces
                //
                // we queue them here because otherwise hasNext() would return false, even if there are super types
                this.queue = new ArrayDeque<>();
                if (ctType.getSuperclass() != null) {
                    this.queue.add(ctType.getSuperclass());
                }
                this.queue.addAll(ctType.getSuperInterfaces());
            }

            @Override
            public boolean hasNext() {
                return !this.queue.isEmpty();
            }

            @Override
            public CtType<?> next() throws NoSuchElementException {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }

                CtTypeReference<?> result = this.queue.pollFirst();
                if (result == null) { // should only happen if we accidentally added null to the queue
                    throw new IllegalStateException("null type reference in queue");
                }

                this.visited.add(result);

                CtTypeReference superClass = result.getSuperclass();
                if (superClass != null) {
                    this.queue.add(superClass);
                }

                // add all super interfaces that we haven't visited yet
                for (CtTypeReference<?> superInterface : result.getSuperInterfaces()) {
                    if (this.visited.add(superInterface)) {
                        this.queue.add(superInterface);
                    }
                }

                // this should never happen, but we check it anyway in case the assumption is wrong
                if (result.getTypeDeclaration() == null) {
                    throw new IllegalStateException("Type declaration is null: " + result.getQualifiedName());
                }

                return result.getTypeDeclaration();
            }
        };
    }

    /**
     * Checks if the given type is equal to any of the expected types.
     *
     * @param ctType the type to check
     * @param expected all allowed types
     * @return true if the given type is equal to any of the expected types, false otherwise
     */
    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, Class<?>... expected) {
        return TypeUtil.isTypeEqualTo(ctType, Arrays.asList(expected));
    }

    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, Collection<Class<?>> expected) {
        TypeFactory factory = ctType.getFactory().Type();
        return TypeUtil.isTypeEqualTo(
            ctType,
            expected.stream()
                .map(factory::get)
                .map(CtType::getReference)
                .toArray(CtTypeReference[]::new)
        );
    }

    /**
     * Checks if the given type is equal to any of the expected types.
     *
     * @param ctType the type to check
     * @param expected all allowed types
     * @return true if the given type is equal to any of the expected types, false otherwise
     */
    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, CtTypeReference<?>... expected) {
        return Arrays.asList(expected).contains(ctType);
    }

    public static boolean isSubtypeOf(CtTypeReference<?> ctTypeReference, Class<?> expected) {
        CtType<?> expectedType = ctTypeReference.getFactory().Type().get(expected);

        if (ctTypeReference.getTypeDeclaration() == null || ctTypeReference instanceof CtTypeParameterReference) {
            return ctTypeReference.isSubtypeOf(expectedType.getReference());
        }

        return UsesFinder.isSubtypeOf(ctTypeReference.getTypeDeclaration(), expectedType);
    }

    /**
     * Checks if the given type is an inner class.
     *
     * @param type the type to check, not null
     * @return true if the given type is an inner class, false otherwise
     */
    public static boolean isInnerClass(CtTypeMember type) {
        return type.getDeclaringType() != null;
    }

    public static boolean isString(CtTypeReference<?> type) {
        return isTypeEqualTo(type, String.class);
    }

    public static boolean isPrimitiveNumeric(CtTypeReference<?> type) {
        return type.isPrimitive()
               && !type.getQualifiedName().equals("boolean")
               && !type.getQualifiedName().equals("char");
    }

    /**
     * Checks if the given element is guaranteed to be immutable.
     * <p>
     * Note that when this method returns {@code false}, the type might still be immutable.
     *
     * @param ctTypeReference the type to check
     * @return true if the given element is guaranteed to be immutable, false otherwise
     * @param <T> the type of the element
     */
    public static <T> boolean isImmutable(CtTypeReference<T> ctTypeReference) {
        Deque<CtTypeReference<?>> queue = new ArrayDeque<>(Collections.singletonList(ctTypeReference));
        Collection<CtType<?>> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            CtType<?> ctType = queue.removeFirst().getTypeDeclaration();

            // if the type is not in the classpath, null is returned
            // in those cases, assume that the type is not immutable
            if (ctType == null) {
                return false;
            }

            // skip types that have been checked (those are guaranteed to be immutable)
            if (visited.contains(ctType)) {
                continue;
            }

            // primitive types and strings are immutable as well:
            if (ctType.getReference().unbox().isPrimitive() || isString(ctType.getReference()) || ctType.isEnum()) {
                continue;
            }

            // types that are not in the classpath like java.util.ArrayList are shadow types.
            // the source code for those is missing, so it is impossible to check if they are immutable.
            // => assume they are not immutable
            if (ctType.isShadow()) {
                return false;
            }

            // for a type to be immutable, all of its fields must be final and immutable as well:
            for (CtFieldReference<?> ctFieldReference : ctType.getAllFields()) {
                if (!VariableUtil.isEffectivelyFinal(ctFieldReference.getFieldDeclaration())) {
                    return false;
                }

                queue.add(ctFieldReference.getType());
            }

            visited.add(ctType);
        }

        return true;
    }

    /**
     * Checks if the given type is a collection that has a defined order.
     *
     * @param ctTypeReference the type to check
     * @return true if the given type is a collection that has a defined order, false if not or if it can not be determined
     * @param <T> the type of the element
     */
    public static <T> boolean isOrderedCollection(CtTypeReference<T> ctTypeReference) {
        return TypeUtil.isSubtypeOf(ctTypeReference, java.util.List.class);
    }
}
