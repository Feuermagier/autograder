package de.firemage.autograder.core.compiler;

import de.firemage.autograder.core.file.UploadedFile;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class Compiler {
    public static final Locale COMPILER_LOCALE = Locale.US;

    private Compiler() {
    }

    public static CompilationResult compileToJar(UploadedFile input, Path tmpLocation, JavaVersion javaVersion)
        throws IOException, CompilationFailureException {
        try {
            return compileWithEncoding(input, tmpLocation, javaVersion, StandardCharsets.UTF_8);
        } catch (CompilationFailureException ex) {
            // Try again with ANSI encoding
            try {
                return compileWithEncoding(input, tmpLocation, javaVersion, StandardCharsets.ISO_8859_1);
            } catch (CompilationFailureException ex2) {
                // Didn't work, the code does really not compile
                throw ex;
            }
        }
    }

    public static CompilationResult compileWithEncoding(UploadedFile input, Path tmpLocation, JavaVersion javaVersion,
                                                        Charset charset)
        throws IOException, CompilationFailureException {

        List<PhysicalFileObject> compilationUnits = input.streamFiles()
            .map(file -> new PhysicalFileObject(file, charset))
            .toList();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path compilerOutput = tmpLocation.resolve(input.getName() + "_compiled");
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StringWriter output = new StringWriter();
        JavaFileManager fileManager = new SeparateBinaryFileManager(
            compiler.getStandardFileManager(diagnosticCollector, Locale.US, charset),
            compilerOutput.toFile(), charset);
        boolean successful = compiler.getTask(
            output,
            fileManager,
            diagnosticCollector,
            Arrays.asList("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial",
                "--release=" + javaVersion.getVersionString()),
            null,
            compilationUnits).call();
        output.flush();
        output.close();

        if (!successful) {
            throw new CompilationFailureException(diagnosticCollector.getDiagnostics().stream()
                .map(d -> new CompilationDiagnostic(d, input.getFile()))
                .toList(), input.getFile());
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Path jar = tmpLocation.resolve(input.getName() + ".jar");
        try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar.toFile()), manifest)) {
            addToJar(compilerOutput.normalize(), compilerOutput.toFile(), jarOut);
        }
        FileUtils.deleteDirectory(compilerOutput.toFile());

        return new CompilationResult(jar, diagnosticCollector.getDiagnostics().stream()
            .map(d -> new CompilationDiagnostic(d, input.getFile()))
            .toList());
    }


    // https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file/1281295#1281295
    private static void addToJar(Path root, File source, JarOutputStream target) throws IOException {
        String relativePath = root.relativize(source.toPath().normalize()).toString().replace("\\", "/");
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!relativePath.isEmpty()) {
                    if (!relativePath.endsWith("/"))
                        relativePath += "/";
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
                    if (count == -1)
                        break;
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
