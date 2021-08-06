package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.InCodeProblem;
import de.firemage.codelinter.linter.PathUtil;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.io.File;

public class SpoonInCodeProblem extends InCodeProblem {
    private final CtElement element;
    private final File root;

    public SpoonInCodeProblem(CtElement element, String description, ProblemCategory category, String explanation, ProblemPriority priority, File root) {
        super(element.getPosition().getFile().getPath(),
                element.getPosition().getLine(),
                element.getPosition().getColumn(),
                description,
                category,
                explanation,
                priority);

        this.element = element;
        this.root = root;
    }

    @Override
    public String getDisplayLocation() {
        if (element == null) {
            return "";
        } else if (element instanceof CtMethod<?> method) {
            return method.getParent(CtClass.class).getQualifiedName() + "#" + method.getSimpleName();
        } else if (element instanceof CtType<?> type) {
            return type.getQualifiedName();
        } else {
            File file = element.getPosition().getFile();
            if (file != null) {
                return PathUtil.getSanitizedPath(file, root) + ":" + element.getPosition().getLine();
            } else {
                return "<UNKNOWN>";
            }
        }
    }
}
