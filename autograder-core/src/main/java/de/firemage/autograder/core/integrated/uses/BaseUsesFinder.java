package de.firemage.autograder.core.integrated.uses;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtPattern;
import spoon.reflect.code.CtResource;
import spoon.reflect.code.CtTypePattern;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.chain.CtQuery;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;

import java.util.List;

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

    public CtQuery query(CtQueryable searchScope) {
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

    // TODO: figure out the overhead for creating a query, maybe this should be cached
    @Override
    public CtQuery query() {
        CtQueryable searchScope = this.toSearchIn;

        if (this.toSearchIn == this.toSearchFor.getFactory().getModel()) {
            switch (this.toSearchFor) {
                // for local variables, one does not need to search the whole model, it is enough to search in the parent block
                case CtLocalVariable<?> ctLocalVariable -> {
                    if (ctLocalVariable.getParent(CtPattern.class) == null) {
                        searchScope = ctLocalVariable.getParent();
                    }
                }
                case CtParameter<?> ctParameter when ctParameter.getParent() instanceof CtExecutable<?> ctExecutable && ctExecutable.getBody() != null -> {
                    searchScope = ctParameter.getParent();
                }
                case CtCatchVariable<?> ctCatchVariable -> {
                    searchScope = ctCatchVariable.getParent();
                }
                case CtResource<?> ctResource -> {
                    searchScope = ctResource.getParent();
                }
                case CtTypeMember ctTypeMember when ctTypeMember.isPrivate() -> {
                    searchScope = ctTypeMember.getDeclaringType();
                }
                case CtTypeMember ctTypeMember when !ctTypeMember.isPublic() -> {
                    searchScope = ctTypeMember.getDeclaringType().getPackage();
                }
                case CtTypeParameter ctTypeParameter -> {
                    searchScope = ctTypeParameter.getParent();
                }
                default -> {
                }
            }
        }

        return this.query(searchScope);
    }
}
