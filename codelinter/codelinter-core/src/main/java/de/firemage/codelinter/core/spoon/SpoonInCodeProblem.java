package de.firemage.codelinter.core.spoon;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.CodePosition;
import de.firemage.codelinter.core.InCodeProblem;
import de.firemage.codelinter.core.PathUtil;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.io.File;

public class SpoonInCodeProblem extends InCodeProblem {
    private final CtElement element;

    public SpoonInCodeProblem(Check check, CtElement element, String explanation) {
        super(check, mapSourceToCode(element.getPosition()), explanation);

        this.element = element;
    }

    private static CodePosition mapSourceToCode(SourcePosition position) {
        // TODO not sure if this is correct
        return new CodePosition(
                position.getFile().toPath(),
                position.getLine(),
                position.getEndLine(),
                position.getColumn(),
                position.getEndColumn()
        );
    }

    /*
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
    */
}
