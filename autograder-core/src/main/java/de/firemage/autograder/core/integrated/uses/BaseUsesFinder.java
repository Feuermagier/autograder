package de.firemage.autograder.core.integrated.uses;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.chain.CtQuery;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;

class BaseUsesFinder<R extends CtElement> implements UsesFinder<R> {
    private final CtElement toSearchFor;
    private CtQueryable toSearchIn;

    private Filter<CtElement> preFilter;
    private Filter<CtElement> postFilter;

    BaseUsesFinder(CtElement toSearchFor) {
        this.toSearchFor = toSearchFor;
        // by default search in the whole model
        this.toSearchIn = toSearchFor.getFactory().getModel();
    }

    /**
     * Narrow the search to only look in the given element.
     *
     * @param toSearchIn the element to search in
     * @return this UsesFinder
     */
    public UsesFinder<R> in(CtQueryable toSearchIn) {
        this.toSearchIn = toSearchIn;
        return this;
    }

    // TODO: allow chaining of pre/post filters

    @SuppressWarnings("unchecked")
    public <I extends CtElement> UsesFinder<R> preFilter(Filter<? super I> preFilter, Class<? super I> itemClass) {
        this.preFilter = new SpoonUtil.FilterAdapter<>(preFilter, (Class<I>) itemClass);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U extends CtElement> UsesFinder<U> postFilter(Filter<? super U> postFilter, Class<? super U> itemClass) {
        this.postFilter = new SpoonUtil.FilterAdapter<>(postFilter, (Class<U>) itemClass);
        return (UsesFinder<U>) this;
    }

    // TODO: figure out the overhead for creating a query, maybe this should be cached
    @Override
    public CtQuery query() {
        CtQueryable searchScope = this.toSearchIn;

        // TODO: other uses that could be optimized:
        // - if the element is not public, we can reduce the search scope to the class/package
        // - if the element is a CtParameter, we can reduce the search scope to the method?
        // - there might be other things that could be optimized

        if (this.toSearchIn == this.toSearchFor.getFactory().getModel()) {
            // for local variables, one does not need to search the whole model, it is enough to search in the parent block
            CtBlock<?> parentBlock = this.toSearchFor.getParent(CtBlock.class);
            if (parentBlock != null && this.toSearchFor instanceof CtLocalVariable<?>) {
                searchScope = parentBlock;
            }
        }

        Filter<CtElement> filter = new SpoonUtil.UsesFilter(this.toSearchFor);

        if (this.preFilter != null) {
            filter = new CompositeFilter<>(FilteringOperator.INTERSECTION, this.preFilter, filter);
        }

        if (this.postFilter != null) {
            filter = new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                filter,
                this.postFilter
            );
        }

        return searchScope.filterChildren(filter);
    }
}
