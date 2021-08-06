package de.firemage.codelinter.core.spoon.check;

import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

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
}
