package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.check.api.CommonReimplementation.CtEnumFieldRead;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = { ProblemType.COLLECTION_ADD_ALL, ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES })
public class CollectionAddAll extends IntegratedCheck {
    private record AddInvocation(
        CtVariableReference<?> collection,
        CtExecutableReference<?> executableReference,
        CtExpression<?> value
    ) {
        public static Optional<AddInvocation> of(CtStatement ctStatement) {
            CtType<?> collectionType = ctStatement.getFactory().Type().get(java.util.Collection.class);
            if (!(ctStatement instanceof CtInvocation<?> ctInvocation)
                || !(ctInvocation.getTarget() instanceof CtVariableAccess<?> ctVariableAccess)
                || ctVariableAccess.getVariable().getType() instanceof CtTypeParameterReference
                || !ctVariableAccess.getVariable().getType().isSubtypeOf(collectionType.getReference())) {
                return Optional.empty();
            }

            CtExecutableReference<?> executableReference = ctInvocation.getExecutable();
            CtVariableReference<?> collection = ctVariableAccess.getVariable();
            if (!SpoonUtil.isSignatureEqualTo(
                executableReference,
                boolean.class,
                "add",
                Object.class)) {
                return Optional.empty();
            }

            return Optional.of(new AddInvocation(collection, executableReference, ctInvocation.getArguments().get(0)));
        }
    }

    private static <T> boolean isOrderedCollection(CtTypeReference<T> ctTypeReference) {
        return Stream.of(java.util.List.class)
            .map(ctClass -> ctTypeReference.getFactory().createCtTypeReference(ctClass))
            .anyMatch(ctTypeReference::isSubtypeOf);
    }

    private void reportProblem(CtVariable<?> ctVariable, List<CtExpression<?>> addedValues) {
        String values = "List.of(%s)".formatted(addedValues.stream()
            .map(CtElement::prettyprint)
            .collect(Collectors.joining(", "))
        );

        List<CtEnumValue<?>> fieldReads = new ArrayList<>();
        CtEnum<?> ctEnum = null;
        for (CtExpression<?> value : addedValues) {
            CtEnumFieldRead fieldRead = CtEnumFieldRead.of(value).orElse(null);
            if (fieldRead == null) {
                ctEnum = null;
                break;
            }

            if (ctEnum == null) {
                ctEnum = fieldRead.ctEnum();
            }

            if (!ctEnum.equals(fieldRead.ctEnum())) {
                ctEnum = null;
                break;
            }

            fieldReads.add(fieldRead.ctEnumValue());
        }

        if (ctEnum != null && CommonReimplementation.checkEnumValues(ctEnum, isOrderedCollection(ctVariable.getType()), fieldReads)) {
            addLocalProblem(
                ctVariable,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s.addAll(Arrays.asList(%s.values()))".formatted(
                            ctVariable.getSimpleName(),
                            ctEnum.getSimpleName()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES
            );
            return;
        }


        addLocalProblem(
            ctVariable,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "%s.addAll(%s)".formatted(ctVariable.getSimpleName(), values)
                )
            ),
            ProblemType.COLLECTION_ADD_ALL
        );
    }

    private void checkAddAll(CtBlock<?> ctBlock) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctBlock);

        CtVariableReference<?> collection = null;
        List<CtExpression<?>> addedValues = new ArrayList<>();

        for (CtStatement ctStatement : statements) {
            AddInvocation addInvocation = AddInvocation.of(ctStatement).orElse(null);
            if (addInvocation == null) {
                if (addedValues.size() > 1 && collection != null) {
                    reportProblem(collection.getDeclaration(), addedValues);
                }

                collection = null;
                continue;
            }

            if (collection == null) {
                collection = addInvocation.collection();
                addedValues.clear();
            }

            // ensure that all invocations refer to the same collection
            if (!collection.equals(addInvocation.collection())) {
                if (addedValues.size() > 1) {
                    reportProblem(collection.getDeclaration(), addedValues);
                }

                // if there are multiple collections, just invalidate the data
                collection = null;
                continue;
            }

            addedValues.add(addInvocation.value());
        }

        if (addedValues.size() > 1 && collection != null) {
            reportProblem(collection.getDeclaration(), addedValues);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBlock<?>>() {
            @Override
            public void process(CtBlock<?> ctBlock) {
                if (ctBlock.isImplicit() || !ctBlock.getPosition().isValidPosition()) {
                    return;
                }

                checkAddAll(ctBlock);
            }
        });
    }
}
