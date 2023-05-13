package de.firemage.autograder.core;

import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.compiler.PhysicalFileObject;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SourceInfo implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SourceInfo.class);

    private final File file;
    private final JavaVersion version;
    private final SerializableCharset charset;

    private SourceInfo(Path file, JavaVersion version, Charset charset) {
        this.file = file.toFile();
        this.version = version;
        this.charset = new SerializableCharset(charset);
    }

    public SourceInfo(Path file, JavaVersion version) throws IOException {
        if (!file.toFile().isDirectory()) {
            throw new IllegalArgumentException("The file must be a directory");
        }

        this.file = file.toFile();
        this.version = version;

        Charset detectedCharset = null;
        for (File javaFile : this.getJavaFiles()) {
            String fileCharsetName = UniversalDetector.detectCharset(javaFile);
            if (fileCharsetName == null) {
                continue;
            }
            Charset fileCharset = Charset.forName(fileCharsetName);
            if (!fileCharset.equals(StandardCharsets.US_ASCII)) {
                if (detectedCharset != null && !fileCharset.equals(detectedCharset)) {
                    throw new IOException("Java files with incompatible encodings found - some are " + detectedCharset +
                        ", but others are " + fileCharset);
                } else {
                    detectedCharset = fileCharset;
                }
            }
        }

        this.charset = new SerializableCharset(Objects.requireNonNullElse(detectedCharset, StandardCharsets.UTF_8));
    }

    public Path getPath() {
        return this.file.toPath().toAbsolutePath();
    }

    public List<File> getJavaFiles() throws IOException {
        return this.streamFiles().toList();
    }

    private Stream<File> streamFiles() throws IOException {
        return Files.walk(this.file.toPath())
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> !p.toString().endsWith("package-info.java"))
            .map(Path::toFile);
    }

    public List<PhysicalFileObject> compilationUnits() throws IOException {
        return this.streamFiles()
            .map(file -> new PhysicalFileObject(file, this.charset))
            .toList();
    }

    public SourceInfo copyTo(Path target) throws IOException {
        FileUtils.copyDirectory(this.file, target.toFile());

        return new SourceInfo(target, this.version, this.charset);
    }

    FileSystemFolder getSpoonFile() {
        return new FileSystemFolder(this.file);
    }

    public List<FileDataSource> getPMDFiles() throws IOException {
        try(Stream<Path> stream = Files.walk(this.file.toPath())) {
            return stream.filter(p -> p.toString().endsWith(".java"))
                .map(p -> new FileDataSource(p.toFile()))
                .toList();
        }
    }

    public String getName() {
        return this.file.toPath().getFileName().toString();
    }

    public void delete() throws IOException {
        FileUtils.deleteDirectory(this.file);
    }

    public JavaVersion getVersion() {
        return version;
    }

    public Charset getCharset() {
        return charset;
    }
}
