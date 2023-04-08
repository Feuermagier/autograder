package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.FactoryAccessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
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

// TODO: see https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/bestpractices/LooseCouplingRule.java

@ExecutableCheck(reportedProblems = { ProblemType.CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE })
public class ConcreteCollectionCheck extends IntegratedCheck {
    private static final List<Class<?>> ALLOWED_TYPES = List.of(java.util.Properties.class);

    public ConcreteCollectionCheck() {
        super(new LocalizedMessage("concrete-collection-desc"));
    }


    // TODO: update concrete-collection-exp/desc in locale.fst

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


        // TODO: what is provides <interface> with <implementation>?

        return false;
    }

    private boolean isInOverriddenMethodSignature(CtTypeReference<?> ctTypeReference) {
        CtMethod<?> ctMethod = ctTypeReference.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        // if the method is defined for the first time, this should return an empty collection
        return !ctMethod.getTopDefinitions().isEmpty();
    }

    // TODO: this is wrong
    private boolean isTypeParameter(CtTypeReference<?> ctTypeReference) {
        return false;
    }

    private boolean checkCtTypeReference(CtTypeReference<?> ctTypeReference) {
        if (this.isConcreteCollectionType(ctTypeReference)
            && !this.isInOverriddenMethodSignature(ctTypeReference)
            && !this.isInAllowedContext(ctTypeReference)
            && !this.isTypeParameter(ctTypeReference)
            && !this.isAllowedType(ctTypeReference)
        ) {
            // TODO: record is visited multiple times for each implicit element
            // A record has both a getter and an attribute -> visited twice and both are implicit...
            CtElement element = ctTypeReference;
            if (!ctTypeReference.getPosition().isValidPosition()) {
                if (ctTypeReference.getParent(CtArrayTypeReference.class) == null) {
                    throw new IllegalStateException("Invalid position for " + ctTypeReference);
                } else {
                    element = ctTypeReference.getParent(CtArrayTypeReference.class);
                }
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
                /*CtElement parent = ctTypeReference;
                while (parent != null) {
                    // do not visit type references in implicit elements
                    if (parent.isImplicit()) {
                        return;
                    }
                    parent = parent.getParent();
                }*/

                boolean hasError = checkCtTypeReference(ctTypeReference);

                if (!hasError) {
                    super.visitCtTypeReference(ctTypeReference);
                }
            }

            @Override
            public <T> void visitCtConstructor(CtConstructor<T> ctConstructor) {
                // do not visit default constructor
                if (!ctConstructor.isImplicit()) {
                    super.visitCtConstructor(ctConstructor);
                }
            }

            @Override
            public void visitCtRecord(CtRecord ctRecord) {
                this.enter(ctRecord);
                this.scan(CtRole.ANNOTATION, ctRecord.getAnnotations());
                this.scan(CtRole.INTERFACE, ctRecord.getSuperInterfaces());
                // this.scan(CtRole.TYPE_MEMBER, ctRecord.getTypeMembers());
                this.scan(CtRole.TYPE_PARAMETER, ctRecord.getFormalCtTypeParameters());
                for (CtRecordComponent component : ctRecord.getRecordComponents()) {
                    this.visitCtField(component.toField());
                }
                this.scan(CtRole.COMMENT, ctRecord.getComments());
                this.exit(ctRecord);
            }
        });
    }
}
