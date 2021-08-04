package de.firemage.codelinter.linter.compiler;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Compiler {
    public static final Locale COMPILER_LOCALE = Locale.US;

    private Compiler() {
    }

    public static CompilationResult createJar(File inputZip, JavaVersion javaVersion) throws IOException, CompilationFailureException {
        String inFileName = inputZip.getName().replace(".zip", "");
        File zipOutput = new File(inputZip.getParentFile(), inFileName + "_unzipped");
        List<PhysicalFileObject> compilationUnits = unzip(inputZip, zipOutput).stream()
                .map(PhysicalFileObject::new)
                .collect(Collectors.toList());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File compilerOutput = new File(inputZip.getParentFile(), inFileName + "_compiled");
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
        FileUtils.deleteDirectory(zipOutput);

        if (!successful) {
            throw new CompilationFailureException(diagnosticCollector.getDiagnostics(), "_unzipped/");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File jar = new File(inputZip.getParentFile(), inFileName + ".jar");
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar));
        addToJar(compilerOutput, jarOut);
        jarOut.close();
        FileUtils.deleteDirectory(compilerOutput);

        return new CompilationResult(jar, diagnosticCollector.getDiagnostics().stream()
                .map(d -> new CompilationDiagnostic(d, "_unzipped/"))
                .collect(Collectors.toList()));
    }

    private static List<File> unzip(File input, File destination) throws IOException {
        List<File> files = new ArrayList<>();
        byte[] buffer = new byte[1024];
        ZipInputStream in = new ZipInputStream(new FileInputStream(input));
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            File newFile = createFileFromZipEntry(destination, entry);
            if (entry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                files.add(newFile);
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            entry = in.getNextEntry();
        }
        in.closeEntry();
        in.close();

        return files;
    }

    private static File createFileFromZipEntry(File destination, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destination, zipEntry.getName());

        String destDirPath = destination.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
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
