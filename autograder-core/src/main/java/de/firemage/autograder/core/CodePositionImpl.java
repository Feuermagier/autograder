package de.firemage.autograder.core;

import de.firemage.autograder.api.CodePosition;
import de.firemage.autograder.api.PathLike;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.SourcePath;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtLoop;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public record CodePositionImpl(SourceInfo sourceInfo, SourcePath file, int startLine, int endLine, int startColumn, int endColumn) implements CodePosition {
    public static CodePositionImpl fromSourcePosition(
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

        // Instead of highlighting all lines of a class or method, only highlight the first line.
        //
        // Someone might explicitly specify a source position, in which case it will differ from the element's position.
        if ((ctElement instanceof CtType<?> || ctElement instanceof CtMethod<?> || ctElement instanceof CtLoop || ctElement instanceof CtAbstractSwitch<?>) && ctElement.getPosition().equals(sourcePosition)) {
            return new CodePositionImpl(
                sourceInfo,
                relativePath,
                sourcePosition.getLine(),
                sourcePosition.getLine(),
                sourcePosition.getColumn(),
                sourcePosition.getColumn()
            );
        }

        return new CodePositionImpl(
            sourceInfo,
            relativePath,
            sourcePosition.getLine(),
            sourcePosition.getEndLine(),
            sourcePosition.getColumn(),
            sourcePosition.getEndColumn()
        );
    }


    @Override
    public Path path() {
        return this.file.toPath();
    }

    @Override
    public String readCompilationUnit() {
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
