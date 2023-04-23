package de.firemage.autograder.core;

import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.UploadedFile;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SourceInfo {
    private static final Logger LOG = LoggerFactory.getLogger(SourceInfo.class);

    private final Path file;
    private final JavaVersion version;
    private final Charset charset;

    private SourceInfo(Path file, JavaVersion version, Charset charset) {
        this.file = file;
        this.version = version;
        this.charset = charset;
    }

    public SourceInfo(Path file, JavaVersion version) throws IOException {
        if (!file.toFile().isDirectory()) {
            throw new IllegalArgumentException("The file must be a directory");
        }

        this.file = file;
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
        this.charset = Objects.requireNonNullElse(detectedCharset, StandardCharsets.UTF_8);
    }

    public Path getFile() {
        return file.toAbsolutePath();
    }

    public List<File> getJavaFiles() throws IOException {
        return streamFiles().toList();
    }

    public Stream<File> streamFiles() throws IOException {
        return Files.walk(this.file)
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> !p.toString().endsWith("package-info.java"))
            .map(Path::toFile);
    }

    public SourceInfo copyTo(Path target) throws IOException {
        FileUtils.copyDirectory(this.file.toFile(), target.toFile());

        return new SourceInfo(target, this.version, this.charset);
    }

    public FileSystemFolder getSpoonFile() {
        return new FileSystemFolder(this.file.toFile());
    }

    public List<FileDataSource> getPMDFiles() throws IOException {
        return Files.walk(this.file)
            .filter(p -> p.toString().endsWith(".java"))
            .map(p -> new FileDataSource(p.toFile()))
            .toList();
    }

    public String getName() {
        return this.file.getFileName().toString();
    }

    public void delete() throws IOException {
        FileUtils.deleteDirectory(this.file.toFile());
    }

    public JavaVersion getVersion() {
        return version;
    }

    public Charset getCharset() {
        return charset;
    }
}
