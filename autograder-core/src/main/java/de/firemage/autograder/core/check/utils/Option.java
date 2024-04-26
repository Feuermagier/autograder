package de.firemage.autograder.core.check.utils;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Option<T> extends Iterable<T> permits Option.Some, Option.None {
    static <T> Option<T> ofNullable(T value) {
        return value == null ? new None<>() : new Some<>(value);
    }

    static <T> Option<T> some(T value) {
        Objects.requireNonNull(value, "Value must not be null.");
        return new Some<>(value);
    }

    static <T> Option<T> none() {
        return new None<>();
    }

    default T unwrap() {
        return switch (this) {
            case Some<T> (var value) -> value;
            case None<T> ignored -> throw new IllegalStateException("Expected Some value, but got None.");
        };
    }

    default boolean isSome() {
        return this instanceof Some;
    }

    default <U> Option<U> map(Function<T, U> function) {
        return switch (this) {
            case Some<T>(var value) -> new Some<>(function.apply(value));
            case None<T> ignored -> new None<>();
        };
    }

    /**
     * Returns the value if it is present or null if it is not.
     *
     * @return the value or null
     */
    default T nullable() {
        return switch (this) {
            case Some<T>(var value) -> value;
            case None<T> ignored -> null;
        };
    }

    default Stream<T> stream() {
        return switch (this) {
            case Some<T>(var value) -> Stream.of(value);
            case None<T> ignored -> Stream.empty();
        };
    }

    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }

    record None<T>() implements Option<T> {
    }

    record Some<T>(T value) implements Option<T> {
    }
}
