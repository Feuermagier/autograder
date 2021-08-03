package de.firemage.codelinter.linter.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Compiler {
    private Compiler() {
    }

    public static CompilerResult createJar(File inputZip) throws IOException {
        String inFileName = inputZip.getName().replace(".zip", "");
        File zipOutput = new File(inputZip.getParentFile(), inFileName + "_unzipped");
        List<File> sourceFiles = unzip(inputZip, zipOutput);

        // https://stackoverflow.com/questions/2946338/how-do-i-programmatically-compile-and-instantiate-a-java-class
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File compilerOutput = new File(inputZip.getParentFile(), inFileName + "_compiled");
        ByteArrayOutputStream standardOut = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOut = new ByteArrayOutputStream();
        List<String> arguments = sourceFiles.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
        arguments.add(0, "-d");
        arguments.add(1, compilerOutput.getAbsolutePath());
        compiler.run(null, null, errorOut, arguments.toArray(String[]::new));

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File jar = new File(inputZip.getParentFile(), inFileName + ".jar");
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar));
        addToJar(compilerOutput, jarOut);
        jarOut.close();

        return new CompilerResult(jar, standardOut.toString(), errorOut.toString());
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
