package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.SourceInfo;
import org.apache.commons.io.FileUtils;

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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public final class Compiler {
    static final Locale COMPILER_LOCALE = Locale.US;

    private Compiler() {
    }

    public static Optional<CompilationResult> compileToJar(SourceInfo input, Path tmpLocation, JavaVersion javaVersion)
        throws IOException, CompilationFailureException {
        return compileAndIgnoreSuppressWarnings(input, tmpLocation, javaVersion);
    }

    // @SuppressWarnings will result in warnings being ignored (obviously). This is suboptimal, when
    // one wants to lint things that the compiler emits like unchecked casts.
    //
    // This piece of code, tries to patch the @SuppressWarnings annotation to not ignore any warnings.
    private static Optional<CompilationResult> compileAndIgnoreSuppressWarnings(
        SourceInfo input, Path tmpLocation, JavaVersion javaVersion
    ) throws IOException, CompilationFailureException {
        Path modifiedOutput = tmpLocation.resolve(input.getName() + "_modified");
        SourceInfo copiedVersion = input.copyTo(modifiedOutput);

        List<PhysicalFileObject> compilationUnits = copiedVersion.compilationUnits();
        // patch the files:
        for (PhysicalFileObject file : compilationUnits) {
            String content = file.getCharContent(true).toString();
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

            try (Writer writer = file.openWriter()) {
                writer.write(patched);
            }
        }

        Optional<CompilationResult> result = compile(copiedVersion, tmpLocation, javaVersion);

        copiedVersion.delete();

        List<CompilationDiagnostic> diagnostics = result.map(CompilationResult::diagnostics).orElse(List.of());

        // now compile the code again, but this time without the patched version (to prevent problems if the patching
        // is broken with the source position)
        return compile(input, tmpLocation, javaVersion)
            .map(res -> new CompilationResult(res.jar(), diagnostics));
    }

    private static Optional<CompilationResult> compile(
        SourceInfo input, Path tmpLocation, JavaVersion javaVersion
    ) throws IOException, CompilationFailureException {

        List<PhysicalFileObject> compilationUnits = input.compilationUnits();

        if (compilationUnits.isEmpty()) {
            return Optional.empty();
        }

        Charset charset = input.getCharset();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path compilerOutput = tmpLocation.resolve(input.getName() + "_compiled");
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StringWriter output = new StringWriter();
        JavaFileManager fileManager = new SeparateBinaryFileManager(
            compiler.getStandardFileManager(diagnosticCollector, Locale.US, charset),
            compilerOutput.toFile(), charset
        );
        boolean successful = compiler.getTask(
            output,
            fileManager,
            diagnosticCollector,
            Arrays.asList("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial",
                "--release=" + javaVersion.getVersionString()
            ),
            null,
            compilationUnits
        ).call();
        output.flush();
        output.close();

        if (!successful) {
            throw new CompilationFailureException(diagnosticCollector.getDiagnostics().stream()
                .map(d -> new CompilationDiagnostic(
                    d,
                    input.getPath()
                ))
                .toList(), input.getPath());
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Path jar = tmpLocation.resolve(input.getName() + ".jar");
        try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar.toFile()), manifest)) {
            addToJar(compilerOutput.normalize(), compilerOutput.toFile(), jarOut);
        }
        FileUtils.deleteDirectory(compilerOutput.toFile());

        return Optional.of(new CompilationResult(jar, diagnosticCollector.getDiagnostics().stream()
            .map(d -> new CompilationDiagnostic(
                d,
                input.getPath()
            ))
            .toList()));
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
