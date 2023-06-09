package de.firemage.autograder.core.check.general;

import com.google.common.collect.Sets;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.filter.DirectReferenceFilter;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@ExecutableCheck(reportedProblems = {
    ProblemType.USE_DIFFERENT_VISIBILITY,
    ProblemType.USE_DIFFERENT_VISIBILITY_PEDANTIC
})
public class UseDifferentVisibility extends IntegratedCheck {
    // returns all parents of a CtElement
    private static Iterable<CtElement> parents(CtElement ctElement) {
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

    private static CtElement findCommonParent(CtElement firstElement, Iterable<? extends CtElement> others) {
        // CtElement::hasParent will recursively call itself until it reaches the root
        // => inefficient and might cause a stack overflow

        // add all parents of the firstElement to a set sorted by distance to the firstElement:
        Set<CtElement> ctParents = new LinkedHashSet<>();
        ctParents.add(firstElement);
        parents(firstElement).forEach(ctParents::add);

        for (CtElement other : others) {
            // only keep the parents that the firstElement and the other have in common
            ctParents.retainAll(Sets.newHashSet(parents(other).iterator()));
        }

        // the first element in the set is the closest common parent
        return ctParents.iterator().next();
    }

    private enum Visibility implements Comparable<Visibility> {
        PRIVATE,
        DEFAULT,
        PROTECTED,
        PUBLIC;

        static Visibility of(CtModifiable ctModifiable) {
            if (ctModifiable.isPublic()) {
                return PUBLIC;
            } else if (ctModifiable.isProtected()) {
                return PROTECTED;
            } else if (ctModifiable.isPrivate()) {
                return PRIVATE;
            } else {
                return DEFAULT;
            }
        }

        boolean isMoreRestrictiveThan(Visibility other) {
            // this < other
            return this.compareTo(other) < 0;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private static Visibility getVisibility(CtTypeMember ctTypeMember, CtReference ctReference) {
        CtModel ctModel = ctReference.getFactory().getModel();

        List<CtReference> references = ctModel.getElements(new DirectReferenceFilter<>(ctReference));

        CtElement commonParent = findCommonParent(ctTypeMember, references);
        CtType<?> declaringType = ctTypeMember.getDeclaringType();

        // if there are no references, the member itself will be returned
        if (ctTypeMember == commonParent) {
            return Visibility.PRIVATE;
        }

        // if all references are in the same type as the member, it can be private
        if (commonParent instanceof CtType<?> ctType
            && (ctType.equals(declaringType)
            // special case for inner classes
            || ctTypeMember.getTopLevelType().equals(ctType))) {
            return Visibility.PRIVATE;
        }

        if (commonParent instanceof CtPackage ctPackage && ctPackage.equals(ctModel.getRootPackage())) {
            if (ctTypeMember.getParent(CtPackage.class).equals(ctPackage)
                && references.stream().allMatch(ref -> ref.getParent(CtPackage.class).equals(ctPackage))) {
                return Visibility.DEFAULT;
            }

            return Visibility.PUBLIC;
        }

        return Visibility.of(ctTypeMember);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypeMember>() {
            @Override
            public void process(CtTypeMember ctTypeMember) {
                if (!ctTypeMember.getPosition().isValidPosition()
                    || ctTypeMember.isImplicit()
                    || ctTypeMember.isPrivate()) {
                    return;
                }

                Visibility currentVisibility = Visibility.of(ctTypeMember);
                Visibility visibility;
                if (ctTypeMember instanceof CtField<?> ctField) {
                    visibility = getVisibility(ctTypeMember, ctField.getType());
                } else if (ctTypeMember instanceof CtMethod<?> ctMethod) {
                    if (SpoonUtil.isMainMethod(ctMethod) || SpoonUtil.isOverriddenMethod(ctMethod)) {
                        return;
                    }

                    visibility = getVisibility(ctTypeMember, ctMethod.getReference());
                } else {
                    return;
                }

                if (visibility.isMoreRestrictiveThan(currentVisibility)) {
                    // it does not make sense to deduct for public things that should be default visibility,
                    // so they are emitted as a different problem type (pedantic)
                    ProblemType problemType = ProblemType.USE_DIFFERENT_VISIBILITY_PEDANTIC;
                    if (visibility == Visibility.PRIVATE) {
                        problemType = ProblemType.USE_DIFFERENT_VISIBILITY;
                    }

                    addLocalProblem(
                        ctTypeMember,
                        new LocalizedMessage(
                            "use-different-visibility",
                            Map.of(
                                "name", ctTypeMember.getSimpleName(),
                                "suggestion", visibility.toString()
                            )
                        ),
                        problemType
                    );
                }
            }
        });
    }
}
