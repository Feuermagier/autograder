package de.firemage.autograder.core.file;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * Represents a compilation unit, which is a source file that can be compiled.
 */
public interface CompilationUnit extends Serializable {
    /**
     * Converts this compilation unit to a {@link JavaFileObject}.
     *
     * @return the {@link JavaFileObject}
     */
    JavaFileObject toJavaFileObject();

    /**
     * Returns the original charset of the compilation unit.
     *
     * @return the charset of the source file
     */
    Charset charset();

    /**
     * Returns the path including the name of the compilation unit.
     * <p>
     * The path must not include the root. For example if the root is {@code src/main/java} and the compilation unit
     * is {@code src/main/java/de/firemage/autograder/core/file/CompilationUnit.java}, the path must be
     * {@code de/firemage/autograder/core/file/CompilationUnit.java}.
     *
     * @return the path of the compilation unit
     * @see SourcePath#resolve(SourcePath)
     */
    SourcePath path();

    /**
     * Returns the content of the compilation unit as a string-
     *
     * @return the content of the compilation unit, never null
     * @throws IOException if the content could not be read
     */
    default String readString() throws IOException {
        JavaFileObject javaFileObject = this.toJavaFileObject();
        CharSequence charSequence = javaFileObject.getCharContent(false);
        if (charSequence == null) {
            throw new IOException("Could not read compilation unit '%s'".formatted(this.path()));
        }

        return charSequence.toString();
    }
}
