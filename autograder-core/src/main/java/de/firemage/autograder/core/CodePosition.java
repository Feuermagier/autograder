package de.firemage.autograder.core;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.nio.file.Path;

public record CodePosition(Path file, int startLine, int endLine, int startColumn, int endColumn) {
    public static CodePosition fromSourcePosition(SourcePosition sourcePosition, CtElement ctElement, Path root) {
        File file = sourcePosition.getFile();
        if (file == null) {
            // Try to find the path in the parent class (if it exists)
            CtType<?> parent = ctElement.getParent(CtType.class);
            if (parent != null) {
                file = parent.getPosition().getFile();
            } else {
                throw new IllegalStateException("Cannot resolve the source file");
            }
        }

        if (ctElement instanceof CtType<?> && ctElement.getPosition().equals(sourcePosition)) {
            return new CodePosition(
                root.relativize(file.toPath()),
                sourcePosition.getLine(),
                sourcePosition.getLine(),
                sourcePosition.getColumn(),
                sourcePosition.getColumn()
            );
        }

        return new CodePosition(
            root.relativize(file.toPath()),
            sourcePosition.getLine(),
            sourcePosition.getEndLine(),
            sourcePosition.getColumn(),
            sourcePosition.getEndColumn()
        );
    }
}
