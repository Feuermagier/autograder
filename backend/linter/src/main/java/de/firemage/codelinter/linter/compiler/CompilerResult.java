package de.firemage.codelinter.linter.compiler;

import java.io.File;

public record CompilerResult(File jar, String output, String errorOutput) {

}
