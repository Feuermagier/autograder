package de.firemage.autograder.core.compiler;

import java.nio.file.Path;
import java.util.List;

public record CompilationResult(Path jar, List<CompilationDiagnostic> diagnostics) {

}
