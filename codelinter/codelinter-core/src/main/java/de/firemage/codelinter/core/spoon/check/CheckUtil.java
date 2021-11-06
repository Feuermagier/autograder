package de.firemage.codelinter.core.spoon.check;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public final class CheckUtil {

    public static boolean isBreakInSwitch(CtCFlowBreak flowBreak) {
        return flowBreak instanceof CtBreak && flowBreak.getParent(CtSwitch.class) != null;
    }

    public static boolean isInLambda(CtElement element) {
        // Check if there is no method between the element and the nearest parent lambda
        CtLambda<?> lambda = element.getParent(CtLambda.class);
        CtMethod<?> method = element.getParent(CtMethod.class);
        return lambda != null && lambda.hasParent(method);
    }

    public static boolean isInEquals(CtElement element) {
        return isEqualsMethod(element.getParent(CtMethod.class));
    }

    public static boolean isEqualsMethod(CtMethod<?> method) {
        if (method == null) {
            return false;
        }

        return method.getSignature().equals("equals(java.lang.Object)")
                && method.getType().unbox().getSimpleName().equals("boolean")
                && method.isPublic()
                && !method.isStatic();
    }

    public static boolean isInMain(CtElement element) {
        return isMainMethod(element.getParent(CtMethod.class));
    }

    public static boolean isMainMethod(CtMethod<?> method) {
        if (method == null) {
            return false;
        }

        return method.getSignature().equals("main(java.lang.String[])")
                && method.getType().unbox().getSimpleName().equals("void")
                && method.isPublic()
                && method.isStatic();
    }

    public static <T> boolean hasGetter(CtClass<?> clazz, CtField<T> field, boolean mustBePublic) {
        Factory factory = clazz.getFactory();
        CtFieldRead<T> read = factory.createFieldRead();
        read.setVariable(field.getReference());

        CtReturn<T> simpleReturnExpression = factory.createReturn();
        simpleReturnExpression.setReturnedExpression(read);
        CtBlock<?> simpleBody = factory.createCtBlock(simpleReturnExpression);

        return clazz.getAllMethods().stream()
                .filter(m -> !mustBePublic || m.isPublic())
                .filter(m -> m.getType().equals(field.getType()))
                .filter(m -> m.getSimpleName().equals("get" + toUpperCamelCase(field.getSimpleName())))
                .filter(m -> m.getParameters().isEmpty())
                .anyMatch(m -> m.getBody().equals(simpleBody));
    }

    public static boolean isException(CtTypeReference<?> type) {
        CtTypeReference<?> currentType = type;
        while (currentType != null && !currentType.getQualifiedName().equals("java.lang.Object")) {
            if (currentType.getQualifiedName().equals("java.lang.Throwable")) {
                return true;
            }
            currentType = currentType.getSuperclass();
        }
        return false;
    }

    public static String toUpperCamelCase(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }
}
