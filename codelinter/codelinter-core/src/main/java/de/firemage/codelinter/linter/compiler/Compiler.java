package de.firemage.codelinter.linter.compiler;

import de.firemage.codelinter.linter.file.UploadedFile;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public final class Compiler {
    public static final Locale COMPILER_LOCALE = Locale.US;

    private Compiler() {
    }

    public static CompilationResult compileToJar(UploadedFile input, JavaVersion javaVersion) throws IOException, CompilationFailureException {
        String inputName = input.getFile().getName();
        File inputParent = input.getFile().getParentFile();

        List<PhysicalFileObject> compilationUnits = input.streamFiles()
                .map(PhysicalFileObject::new)
                .collect(Collectors.toList());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File compilerOutput = new File(inputParent, inputName + "_compiled");
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StringWriter output = new StringWriter();
        JavaFileManager fileManager = new SeparateBinaryFileManager(
                compiler.getStandardFileManager(diagnosticCollector, Locale.US, StandardCharsets.UTF_8),
                compilerOutput);
        boolean successful = compiler.getTask(
                output,
                fileManager,
                diagnosticCollector,
                Arrays.asList("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial", "--release=" + javaVersion.getVersionString()),
                null,
                compilationUnits).call();
        output.flush();
        output.close();

        if (!successful) {
            throw new CompilationFailureException(diagnosticCollector.getDiagnostics(), input.getFile());
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File jar = new File(inputParent, inputName + ".jar");
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar));
        addToJar(compilerOutput, jarOut);
        jarOut.close();
        FileUtils.deleteDirectory(compilerOutput);

        return new CompilationResult(jar, diagnosticCollector.getDiagnostics().stream()
                .map(d -> new CompilationDiagnostic(d, input.getFile()))
                .collect(Collectors.toList()));
    }


    // https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file/1281295#1281295
    private static void addToJar(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles())
                    addToJar(nestedFile, target);
                return;
            }

            JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
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
        } finally {
            if (in != null)
                in.close();
        }
    }
}
