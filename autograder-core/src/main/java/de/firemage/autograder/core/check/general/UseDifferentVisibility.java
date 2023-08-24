package de.firemage.autograder.core.check.general;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExecutableCheck(reportedProblems = {
    ProblemType.USE_DIFFERENT_VISIBILITY,
    ProblemType.USE_DIFFERENT_VISIBILITY_PEDANTIC
})
public class UseDifferentVisibility extends IntegratedCheck {
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

    private static Visibility getVisibility(CtTypeMember ctTypeMember) {
        CtModel ctModel = ctTypeMember.getFactory().getModel();

        Set<CtElement> references = SpoonUtil.findUses(ctTypeMember);

        CtElement commonParent = SpoonUtil.findCommonParent(ctTypeMember, references);
        CtType<?> declaringType = ctTypeMember.getDeclaringType();

        // if there are no references, the member itself will be returned
        if (ctTypeMember == commonParent) {
            return Visibility.of(ctTypeMember);
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
                if (ctTypeMember instanceof CtMethod<?> ctMethod && (SpoonUtil.isMainMethod(ctMethod) || SpoonUtil.isOverriddenMethod(ctMethod))) {
                    return;
                }

                // only check methods and fields
                Visibility visibility;
                if (ctTypeMember instanceof CtMethod<?> ctMethod) {
                    visibility = getVisibility(ctMethod);
                } else if (ctTypeMember instanceof CtField<?> ctField) {
                    visibility = getVisibility(ctField);

                    // special case for fields that are referenced by other fields in the same class
                    // For more details see the test case TestUseDifferentVisibility#testBackwardReference
                    Optional<Visibility> referencingVisibility = ctField.getDeclaringType()
                        .getFields()
                        .stream()
                        // filter out the field itself and those that do not reference the field
                        .filter(field -> field != ctField && !SpoonUtil.findUsesIn(ctField, field).isEmpty())
                        .map(UseDifferentVisibility::getVisibility)
                        .max(Visibility::compareTo);

                    if (referencingVisibility.isPresent() && visibility.isMoreRestrictiveThan(referencingVisibility.get())) {
                        visibility = referencingVisibility.get();
                    }
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
