package de.firemage.autograder.core.integrated;

import spoon.reflect.declaration.CtElement;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A stream of Spoon elements. Implements utility methods for elements.
 * Compatible with standard Java Streams, in contrast to Spoon's CtQuery.
 *
 * @noinspection NullableProblems
 */
public class CtElementStream<T extends CtElement> implements Stream<T> {
    private final Stream<T> baseStream;

    public static <T extends CtElement> CtElementStream<T> fromStream(Stream<T> stream) {
        return new CtElementStream<>(stream);
    }

    public static <T extends CtElement> CtElementStream<T> of(T element) {
        return new CtElementStream<>(Stream.of(element));
    }

    @SafeVarargs
    public static <T extends CtElement> CtElementStream<T> of(T... elements) {
        return new CtElementStream<>(Stream.of(elements));
    }

    public static <T extends CtElement> CtElementStream<T> of(Iterable<T> elements) {
        return new CtElementStream<>(StreamSupport.stream(elements.spliterator(), false));
    }

    public static <T extends CtElement> CtElementStream<T> empty() {
        return new CtElementStream<>(Stream.empty());
    }

    public static <T extends CtElement> CtElementStream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        return new CtElementStream<>(Stream.concat(a, b));
    }

    private CtElementStream(Stream<T> baseStream) {
        this.baseStream = baseStream;
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// New Methods //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Like Stream::map, but returns a CtElementStream instead of a Stream.
     * @param function
     * @return
     * @param <R>
     */
    public <R extends CtElement> CtElementStream<R> mapToElement(Function<? super T, ? extends R> function) {
        return new CtElementStream<>(baseStream.map(function));
    }

    /**
     * Filters the stream to only include elements of the given type.
     * @param type
     * @return
     * @param <R>
     */
    public <R extends T> CtElementStream<R> ofType(Class<R> type) {
        return new CtElementStream<>(baseStream.filter(type::isInstance).map(type::cast));
    }

    /**
     * Assumes that all elements in the stream are of the given type.
     * This method is potentially unsafe and may throw class cast exceptions at runtime!
     * Use ofType() for a safer alternative.
     * @return
     * @param <R>
     */
    @SuppressWarnings("unchecked")
    public <R extends T> CtElementStream<R> assumeElementType() {
        return (CtElementStream<R>) this;
    }

    /**
     * Casts this stream to a stream of CtElements.
     * @return
     */
    @SuppressWarnings("unchecked")
    public CtElementStream<CtElement> asUntypedStream() {
        return (CtElementStream<CtElement>) this;
    }

    /**
     * Filters the stream to only include elements that are direct children of the given parent.
     * @param parent
     * @return
     */
    public CtElementStream<T> withDirectParent(CtElement parent) {
        return new CtElementStream<>(baseStream.filter(e -> e.getParent() == parent));
    }

    /**
     * Filters the stream to only include elements that have a direct parent of the given type.
     * @param parent
     * @return
     */
    public CtElementStream<T> withDirectParent(Class<? extends CtElement> parent) {
        return new CtElementStream<>(baseStream.filter(e -> parent.isInstance(e.getParent())));
    }

    /**
     * Filters the stream to only include elements that are nested in the given parent, or are equal to the parent.
     * @param parent
     * @return
     */
    public CtElementStream<T> nestedIn(CtElement parent) {
        return this.filter(e -> SpoonUtil.isNestedOrSame(e, parent));
    }

    /**
     * Filters the stream to only include elements that are nested in an element of the given type, or are of the given type.
     * @param parentType
     * @return
     */
    public CtElementStream<T> nestedIn(Class<? extends CtElement> parentType) {
        return this.filter(e -> parentType.isInstance(e) || e.getParent(parentType) != null);
    }

    /**
     * Filters the stream to only include elements that are nested in an element of the given type, or are of the given type.
     * @param parentType
     * @return
     */
    public CtElementStream<T> notNestedIn(Class<? extends CtElement> parentType) {
        return this.filter(e -> !parentType.isInstance(e) && e.getParent(parentType) == null);
    }

    /**
     * Checks whether the stream contains any elements.
     * @return
     */
    public boolean hasAny() {
        return baseStream.findAny().isPresent();
    }

    /**
     * Checks whether the stream contains no elements.
     * @return
     */
    public boolean hasNone() {
        return baseStream.findAny().isEmpty();
    }

    /**
     * Filters the stream's elements by their direct parent.
     * @param filter
     * @return
     */
    public CtElementStream<T> filterDirectParent(Predicate<? super CtElement> filter) {
        return new CtElementStream<>(baseStream.filter(e -> filter.test(e.getParent())));
    }

    /**
     * Filters the stream's elements by their direct parent's type and the given filter.
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public <P extends CtElement> CtElementStream<T> filterDirectParent(Class<P> parentType, Predicate<P> filter) {
        return new CtElementStream<>(baseStream.filter(e -> parentType.isInstance(e.getParent()) && filter.test((P) e.getParent())));
    }

    /**
     * Filter the stream's elements by their (possibly indirect) parent of the given type.
     * Beware that the elements passed to the filter may be null if an element has no parent of the given type.
     * @param parentType
     * @param filter
     * @return
     * @param <P>
     */
    public <P extends CtElement> CtElementStream<T> filterIndirectParent(Class<P> parentType, Predicate<? super CtElement> filter) {
        return new CtElementStream<>(baseStream.filter(e -> filter.test(e.getParent(parentType))));
    }

    /**
     * Turns the stream into an iterable.
     * BE CAREFUL: You can only iterate over the iterable once!
     * There's a reason why iterable() isn't implemented for standard Java Streams.
     * However, it's useful, so we implement it here and advice you to be cautious.
     * @return
     */
    public Iterable<T> iterable() {
        return baseStream::iterator;
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////// Delegated Methods from Stream<T> ///////////////////////
    /////////////////////////////////////////////////////////////////////////////
    @Override
    public CtElementStream<T> filter(Predicate<? super T> predicate) {
        return new CtElementStream<>(baseStream.filter(predicate));
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> function) {
        return baseStream.map(function);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> toIntFunction) {
        return baseStream.mapToInt(toIntFunction);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> toLongFunction) {
        return baseStream.mapToLong(toLongFunction);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> toDoubleFunction) {
        return baseStream.mapToDouble(toDoubleFunction);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> function) {
        return baseStream.flatMap(function);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> function) {
        return baseStream.flatMapToInt(function);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> function) {
        return baseStream.flatMapToLong(function);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> function) {
        return baseStream.flatMapToDouble(function);
    }

    @Override
    public <R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        return baseStream.mapMulti(mapper);
    }

    @Override
    public IntStream mapMultiToInt(BiConsumer<? super T, ? super IntConsumer> mapper) {
        return baseStream.mapMultiToInt(mapper);
    }

    @Override
    public LongStream mapMultiToLong(BiConsumer<? super T, ? super LongConsumer> mapper) {
        return baseStream.mapMultiToLong(mapper);
    }

    @Override
    public DoubleStream mapMultiToDouble(BiConsumer<? super T, ? super DoubleConsumer> mapper) {
        return baseStream.mapMultiToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return baseStream.distinct();
    }

    @Override
    public Stream<T> sorted() {
        return baseStream.sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return baseStream.sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> consumer) {
        return baseStream.peek(consumer);
    }

    @Override
    public Stream<T> limit(long l) {
        return baseStream.limit(l);
    }

    @Override
    public Stream<T> skip(long l) {
        return baseStream.skip(l);
    }

    @Override
    public Stream<T> takeWhile(Predicate<? super T> predicate) {
        return baseStream.takeWhile(predicate);
    }

    @Override
    public Stream<T> dropWhile(Predicate<? super T> predicate) {
        return baseStream.dropWhile(predicate);
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
        baseStream.forEach(consumer);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> consumer) {
        baseStream.forEachOrdered(consumer);
    }

    @Override
    public Object[] toArray() {
        return baseStream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> intFunction) {
        return baseStream.toArray(intFunction);
    }

    @Override
    public T reduce(T t, BinaryOperator<T> binaryOperator) {
        return baseStream.reduce(t, binaryOperator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> binaryOperator) {
        return baseStream.reduce(binaryOperator);
    }

    @Override
    public <U> U reduce(U u, BiFunction<U, ? super T, U> biFunction, BinaryOperator<U> binaryOperator) {
        return baseStream.reduce(u, biFunction, binaryOperator);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> biConsumer, BiConsumer<R, R> biConsumer1) {
        return baseStream.collect(supplier, biConsumer, biConsumer1);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return baseStream.collect(collector);
    }

    @Override
    public List<T> toList() {
        return baseStream.toList();
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return baseStream.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return baseStream.max(comparator);
    }

    @Override
    public long count() {
        return baseStream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return baseStream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return baseStream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return baseStream.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return baseStream.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return baseStream.findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return baseStream.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return baseStream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return baseStream.isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return baseStream.sequential();
    }

    @Override
    public Stream<T> parallel() {
        return baseStream.parallel();
    }

    @Override
    public Stream<T> unordered() {
        return baseStream.unordered();
    }

    @Override
    public Stream<T> onClose(Runnable runnable) {
        return baseStream.onClose(runnable);
    }

    @Override
    public void close() {
        baseStream.close();
    }
}
