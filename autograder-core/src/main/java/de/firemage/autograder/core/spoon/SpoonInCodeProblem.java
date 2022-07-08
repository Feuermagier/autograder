package de.firemage.autograder.core.spoon;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.InCodeProblem;
import de.firemage.autograder.core.check.Check;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

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
