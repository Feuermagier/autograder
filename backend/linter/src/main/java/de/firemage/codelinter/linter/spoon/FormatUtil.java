package de.firemage.codelinter.linter.spoon;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;

public final class FormatUtil {
    private FormatUtil() {
    }

    public static String formatPath(CtElement element) {
        if (element == null) {
            return "";
        } else if (element instanceof CtMethod<?> method) {
            return method.getParent(CtClass.class).getQualifiedName() + "#" + method.getSimpleName();
        } else if (element instanceof CtType<?> type) {
            return type.getQualifiedName();
        } else {
            File file = element.getPosition().getFile();
            if (file != null) {
                return file.getPath() + ":" + element.getPosition().getLine();
            } else {
                return "<UNKNOWN>";
            }
        }
    }
}
