package de.firemage.codelinter.linter.compiler;

import java.io.File;
import java.util.List;

public record CompilationResult(File jar, List<CompilationDiagnostic> diagnostics) {

}
