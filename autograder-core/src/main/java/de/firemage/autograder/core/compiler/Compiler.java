package de.firemage.autograder.core.compiler;

import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.api.TempLocation;
import de.firemage.autograder.core.file.CompilationUnit;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.api.TempLocation;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public record Compiler(TempLocation tempLocation, JavaVersion javaVersion) {
    static final Locale COMPILER_LOCALE = Locale.US;

    public Optional<CompilationResult> compileToJar(SourceInfo input) throws IOException, CompilationFailureException {
        return this.compileAndIgnoreSuppressWarnings(input);
    }

    // @SuppressWarnings will result in warnings being ignored (obviously). This is suboptimal, when
    // one wants to lint things that the compiler emits like unchecked casts.
    //
    // This piece of code, tries to patch the @SuppressWarnings annotation to not ignore any warnings.
    private Optional<CompilationResult> compileAndIgnoreSuppressWarnings(
        SourceInfo input
    ) throws IOException, CompilationFailureException {
        Optional<CompilationResult> compilationResult;
        try (TempLocation modifiedOutput = this.tempLocation.createTempDirectory(input.getName() + "_modified")) {
            SourceInfo copiedVersion = input.copyTo(modifiedOutput.toPath());

            List<CompilationUnit> compilationUnits = copiedVersion.compilationUnits();
            // patch the files:
            for (CompilationUnit file : compilationUnits) {
                JavaFileObject javaFileObject = file.toJavaFileObject();
                String content = file.readString();
                Pattern pattern = Pattern.compile("@SuppressWarnings\\((.+?)\\)", Pattern.DOTALL);
                String patched = pattern.matcher(content).replaceAll(matchResult -> {
                    String group = matchResult.group(1);
                    String result = "";
                    int i = 0;
                    int length = group.length();
                    for (char c : group.toCharArray()) {
                        if (i == 0) {
                            result += '{';
                        } else if (i == length - 1) {
                            result += '}';
                        } else if (c == '\r' || c == '\n') {
                            result += c;
                        } else {
                            result += ' ';
                        }

                        i++;
                    }

                    return "@SuppressWarnings(%s)".formatted(result);
                });

                try (Writer writer = javaFileObject.openWriter()) {
                    writer.write(patched);
                }
            }

            compilationResult = this.compile(copiedVersion);
        }

        List<CompilationDiagnostic> diagnostics = compilationResult.map(CompilationResult::diagnostics).orElse(List.of());

        // now compile the code again, but this time without the patched version (to prevent problems if the patching
        // is broken with the source position)
        return this.compile(input).map(res -> new CompilationResult(res.jar(), diagnostics));
    }

    private Optional<CompilationResult> compile(SourceInfo input) throws IOException, CompilationFailureException {

        List<CompilationUnit> compilationUnits = input.compilationUnits();

        if (compilationUnits.isEmpty()) {
            return Optional.empty();
        }

        // TODO: charset should be for each file individually, this requires changing SeparateBinaryFileManager
        Charset charset = compilationUnits.get(0).charset();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StringWriter output = new StringWriter();

        List<CompilationDiagnostic> diagnostics = new ArrayList<>();

        Path jar;
        try (TempLocation compilerOutput = this.tempLocation.createTempDirectory(input.getName() + "_compiled")) {
            JavaFileManager fileManager = new SeparateBinaryFileManager(
                compiler.getStandardFileManager(diagnosticCollector, Locale.US, charset),
                compilerOutput.toPath().toFile(),
                charset
            );

            boolean isSuccessful = compiler.getTask(
                output,
                fileManager,
                diagnosticCollector,
                Arrays.asList("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial",
                    "--release=" + javaVersion.getVersionString()
                ),
                null,
                compilationUnits.stream().map(CompilationUnit::toJavaFileObject).toList()
            ).call();

            output.flush();
            output.close();

            diagnostics.addAll(diagnosticCollector.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getSource() != null)
                .map(diagnostic -> new CompilationDiagnostic(diagnostic, input))
                .toList());

            if (!isSuccessful) {
                throw new CompilationFailureException(diagnostics);
            }

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            jar = this.tempLocation.createTempFile(input.getName() + ".jar");
            try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar.toFile()), manifest)) {
                addToJar(compilerOutput.toPath().normalize(), compilerOutput.toPath().toFile(), jarOut);
            }
        }

        return Optional.of(new CompilationResult(jar, diagnostics));
    }


    // https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file/1281295#1281295
    private static void addToJar(Path root, File source, JarOutputStream target) throws IOException {
        String relativePath = root.relativize(source.toPath().normalize()).toString().replace("\\", "/");
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!relativePath.isEmpty()) {
                    if (!relativePath.endsWith("/")) {
                        relativePath += "/";
                    }
                    JarEntry entry = new JarEntry(relativePath);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles()) {
                    addToJar(root, nestedFile, target);
                }
            } else {
                JarEntry entry = new JarEntry(relativePath);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
