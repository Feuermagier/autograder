package de.firemage.autograder.core.integrated;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Arrays;
import java.util.List;

public final class MethodUtil {
    private MethodUtil() {
    }

    public static boolean isMainMethod(CtMethod<?> method) {
        return method.isStatic()
            && method.isPublic()
            && MethodUtil.isSignatureEqualTo(
            method.getReference(),
            void.class,
            "main",
            java.lang.String[].class
        );
    }

    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        Class<?> returnType,
        String methodName,
        Class<?>... parameterTypes
    ) {
        TypeFactory factory = ctExecutableReference.getFactory().Type();
        return MethodUtil.isSignatureEqualTo(
            ctExecutableReference,
            factory.createReference(returnType),
            methodName,
            Arrays.stream(parameterTypes).map(factory::createReference).toArray(CtTypeReference[]::new)
        );
    }

    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        CtTypeReference<?> returnType,
        String methodName,
        CtTypeReference<?>... parameterTypes
    ) {
        // check that they both return the same type
        if (!TypeUtil.isTypeEqualTo(ctExecutableReference.getType(), returnType)) {
            return false;
        }

        // their names should match:
        if (!ctExecutableReference.getSimpleName().equals(methodName)) {
            return false;
        }

        List<CtTypeReference<?>> givenParameters = ctExecutableReference.getParameters();

        // the number of parameters should match
        if (givenParameters.size() != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            // check if the type of the parameter is equal to the expected type
            if (!TypeUtil.isTypeEqualTo(givenParameters.get(i), parameterTypes[i])) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInOverridingMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return MethodHierarchy.isOverridingMethod(ctMethod);
    }

    /**
     * Checks if the given method is an invocation.
     * @param statement which is checked
     * @return true if the statement is an invocation (instance of CtInvocation, CtConstructorCall or CtLambda),
     * false otherwise
     */
    public static boolean isInvocation(CtStatement statement) {
        return statement instanceof CtInvocation<?> || statement instanceof CtConstructorCall<?> ||
            statement instanceof CtLambda<?>;
    }

    public static boolean isInMainMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return MethodUtil.isMainMethod(ctMethod);
    }
}
