package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.FactoryAccessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtRecordComponent;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.List;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = { ProblemType.CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE })
public class ConcreteCollectionCheck extends IntegratedCheck {
    private static final List<Class<?>> ALLOWED_TYPES = List.of(java.util.Properties.class);

    private <T extends CtTypeInformation & FactoryAccessor> boolean isConcreteCollectionType(T ctType) {
        return Stream.of(java.util.Collection.class, java.util.Map.class)
                     .map(ty -> ctType.getFactory().Type().createReference(ty, false))
                     .anyMatch(ctType::isSubtypeOf) && !ctType.isInterface();
    }

    private boolean isAllowedType(CtTypeReference<?> ctTypeReference) {
        return ALLOWED_TYPES.stream()
                            .map(ty -> ctTypeReference.getFactory().Type().createReference(ty, false))
                            .anyMatch(ctTypeReference::equals);
    }

    private boolean isInAllowedContext(CtTypeReference<?> ctTypeReference) {
        // new ArrayList<>()
        CtConstructorCall<?> ctConstructorCall = ctTypeReference.getParent(CtConstructorCall.class);
        if (ctConstructorCall != null && ctTypeReference.equals(ctConstructorCall.getType())) {
            return true;
        }

        // ArrayList::new
        if (ctTypeReference.getParent(CtExecutableReferenceExpression.class) != null
            || ctTypeReference.getParent(CtExecutableReference.class) != null) {
            return true;
        }

        // calling (static) method
        CtInvocation<?> ctInvocation = ctTypeReference.getParent(CtInvocation.class);
        if (ctInvocation != null
            && ctInvocation.getTarget() != null
            && ctInvocation.getTarget().equals(ctTypeReference.getParent(CtTypeAccess.class))) {
            return true;
        }

        // new ArrayList[10]
        if (ctTypeReference.getParent(CtNewArray.class) != null) {
            return true;
        }

        // (ArrayList<?>) myList
        CtVariable<?> ctVariable = ctTypeReference.getParent(CtVariable.class);
        if (ctVariable != null
            && ctVariable.getDefaultExpression() != null
            && ctVariable.getDefaultExpression().getTypeCasts().contains(ctTypeReference)) {
            return true;
        }

        CtVariableRead<?> ctVariableRead = ctTypeReference.getParent(CtVariableRead.class);
        if (ctVariableRead != null && ctVariableRead.getTypeCasts().contains(ctTypeReference)) {
            return true;
        }

        // ArrayList.class
        CtFieldRead<?> ctFieldRead = ctTypeReference.getParent(CtFieldRead.class);
        if (ctFieldRead != null) {
            CtFieldReference<?> ctFieldReference = ctFieldRead.getVariable();
            return ctFieldReference.getDeclaringType().equals(ctTypeReference)
                   && ctFieldReference.getType()
                                      .equals(ctTypeReference.getFactory().Type().createReference(Class.class, false));
        }

        // AbstractMap.SimpleEntry
        CtTypeReference<?> parentTypeReference = ctTypeReference.getParent(CtTypeReference.class);
        if (parentTypeReference != null) {
            return ctTypeReference.equals(parentTypeReference.getDeclaringType());
        }

        // variable instanceof ArrayList<?>
        CtBinaryOperator<?> ctBinaryOperator = ctTypeReference.getParent(CtBinaryOperator.class);
        if (ctBinaryOperator != null) {
            return ctBinaryOperator.getKind() == BinaryOperatorKind.INSTANCEOF;
        }

        // MyClass<T> extends ArrayList<T>
        CtElement parent = ctTypeReference.getParent();
        if (parent == null) {
            return false;
        }

        if (parent instanceof CtClass<?> parentType && parentType.getSuperclass() != null) {
            return parentType.getSuperclass().equals(ctTypeReference);
        }

        // MyList.this.method() and MyList.super.method()
        // for MyList extends ArrayList implements List
        if (ctTypeReference.getParent(CtSuperAccess.class) != null || ctTypeReference.getParent(CtThisAccess.class) != null) {
            return true;
        }

        return false;
    }

    private boolean checkCtTypeReference(CtTypeReference<?> ctTypeReference) {
        if (this.isConcreteCollectionType(ctTypeReference)
            && !SpoonUtil.isInOverriddenMethod(ctTypeReference)
            && !this.isInAllowedContext(ctTypeReference)
            && !this.isAllowedType(ctTypeReference)
        ) {
            // A record has both a getter and an attribute -> visited twice and both are implicit...
            CtElement element = ctTypeReference;
            while (!element.getPosition().isValidPosition()
                && (element.getParent(CtArrayTypeReference.class) != null)) {
                    element = element.getParent(CtArrayTypeReference.class);
            }

            this.addLocalProblem(
                element,
                new LocalizedMessage("concrete-collection-exp"),
                ProblemType.CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE
            );

            return true;
        }

        return false;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        // Checks for fields, parameters and return types
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtTypeReference(CtTypeReference<T> ctTypeReference) {
                // check for var
                CtLocalVariable<?> ctLocalVariable = ctTypeReference.getParent(CtLocalVariable.class);
                if (ctLocalVariable != null && ctLocalVariable.isInferred()) {
                    return;
                }

                if (!ctTypeReference.getPosition().isValidPosition()
                    // arrays are special, they will be handled by the code
                    && (ctTypeReference.getParent(CtArrayTypeReference.class) == null)) {
                    return;
                }

                boolean hasError = checkCtTypeReference(ctTypeReference);

                if (!hasError) {
                    super.visitCtTypeReference(ctTypeReference);
                }
            }

            @Override
            public void visitCtRecord(CtRecord ctRecord) {
                this.enter(ctRecord);
                this.scan(CtRole.ANNOTATION, ctRecord.getAnnotations());
                this.scan(CtRole.INTERFACE, ctRecord.getSuperInterfaces());
                this.scan(CtRole.TYPE_PARAMETER, ctRecord.getFormalCtTypeParameters());
                ctRecord.getFields().forEach(this::visitCtField);
                this.scan(CtRole.COMMENT, ctRecord.getComments());
                this.exit(ctRecord);
            }
        });
    }
}
