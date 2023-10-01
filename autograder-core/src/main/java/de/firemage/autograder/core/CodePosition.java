package de.firemage.autograder.core;

import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.SourcePath;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;

public record CodePosition(SourceInfo sourceInfo, SourcePath file, int startLine, int endLine, int startColumn, int endColumn) {
    public static CodePosition fromSourcePosition(
        SourcePosition sourcePosition,
        CtElement ctElement,
        SourceInfo sourceInfo
    ) {
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

        SourcePath relativePath = sourceInfo.getCompilationUnit(file.toPath()).path();

        if (ctElement instanceof CtType<?> && ctElement.getPosition().equals(sourcePosition)) {
            return new CodePosition(
                sourceInfo,
                relativePath,
                sourcePosition.getLine(),
                sourcePosition.getLine(),
                sourcePosition.getColumn(),
                sourcePosition.getColumn()
            );
        }

        return new CodePosition(
            sourceInfo,
            relativePath,
            sourcePosition.getLine(),
            sourcePosition.getEndLine(),
            sourcePosition.getColumn(),
            sourcePosition.getEndColumn()
        );
    }

    public String readString() {
        try {
            return this.sourceInfo.getCompilationUnit(this.file).readString();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read source file", exception);
        }
    }

    @Override
    public String toString() {
        return "%s:L%d:%d".formatted(this.file, this.startLine, this.startColumn);
    }
}
