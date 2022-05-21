package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.check.Check;
import de.firemage.codelinter.core.CodePosition;
import de.firemage.codelinter.core.InCodeProblem;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import java.io.File;
import java.nio.file.Path;

public class IntegratedInCodeProblem extends InCodeProblem {
    private final CtElement element;

    public IntegratedInCodeProblem(Check check, CtElement element, String explanation, Path root) {
        super(check, mapSourceToCode(element, root), explanation);

        this.element = element;
    }

    private static CodePosition mapSourceToCode(CtElement element, Path root) {
        SourcePosition position = element.getPosition();
        File file = position.getFile();
        if (file == null) {
            // Try to find the path in the parent class (if it exists)
            CtClass<?> parent = element.getParent(CtClass.class);
            if (parent != null) {
                file = parent.getPosition().getFile();
            } else {
                throw new IllegalStateException("Cannot resolve the source file");
            }
        }
        return new CodePosition(
                root.relativize(file.toPath()),
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
