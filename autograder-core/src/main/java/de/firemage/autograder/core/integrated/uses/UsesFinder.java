package de.firemage.autograder.core.integrated.uses;

import de.firemage.autograder.core.check.utils.Option;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.chain.CtConsumableFunction;
import spoon.reflect.visitor.chain.CtFunction;
import spoon.reflect.visitor.chain.CtQuery;
import spoon.reflect.visitor.chain.CtQueryable;

import java.util.List;

public interface UsesFinder<R extends CtElement> extends CtQueryable {
    static UsesFinder<CtElement> ofElement(CtElement toSearchFor) {
        return new BaseUsesFinder<>(toSearchFor);
    }

    static UsesFinder<CtElement> of(CtTypeMember ctTypeMember) {
        return UsesFinder.ofElement(ctTypeMember);
    }

    static <T> UsesFinder<CtVariableAccess<T>> of(CtVariable<T> toSearchFor) {
        return UsesFinder.ofElement(toSearchFor).filterType(CtVariableAccess.class);
    }

    static <T> UsesFinder<CtVariableRead<T>> ofVariableRead(CtVariable<T> toSearchFor) {
        return UsesFinder.ofElement(toSearchFor).filterType(CtVariableRead.class);
    }


    static <T> UsesFinder<CtVariableWrite<T>> ofVariableWrite(CtVariable<T> toSearchFor) {
        return UsesFinder.ofElement(toSearchFor).filterType(CtVariableWrite.class);
    }

    UsesFinder<R> in(CtQueryable toSearchIn);

    <I extends CtElement> UsesFinder<R> preFilter(Filter<? super I> preFilter, Class<? super I> itemClass);

    default UsesFinder<R> preFilter(Filter<? super CtElement> preFilter) {
        return this.preFilter(preFilter, CtElement.class);
    }

    <U extends CtElement> UsesFinder<U> postFilter(Filter<? super U> postFilter, Class<? super U> itemClass);

    default <U extends CtElement> UsesFinder<U> filterType(Class<? super U> itemClass) {
        return this.postFilter(element -> true, itemClass);
    }

    CtQuery query();

    /**
     * Finds all uses of the element and returns them as a list.
     *
     * @return all uses of the element, you must ensure that the elements are really of type R (if necessary use filterType)
     */
    default List<R> all() {
        return this.query().list();
    }

    default Option<R> any() {
        return Option.ofNullable(this.query().first());
    }

    default boolean hasAny() {
        return this.any().isSome();
    }

    default Option<R> anyMatch(Filter<R> filter) {
        return Option.ofNullable(this.query().filterChildren(filter).first());
    }

    default boolean hasAnyMatch(Filter<R> filter) {
        return this.anyMatch(filter).isSome();
    }

    @Override
    default <R extends CtElement> CtQuery filterChildren(Filter<R> filter) {
        return this.query().filterChildren(filter);
    }

    @Override
    default <I, R> CtQuery map(CtFunction<I, R> function) {
        return this.query().map(function);
    }

    @Override
    default <I> CtQuery map(CtConsumableFunction<I> queryStep) {
        return this.query().map(queryStep);
    }
}
