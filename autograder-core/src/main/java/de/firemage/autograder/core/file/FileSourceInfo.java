package de.firemage.autograder.core.file;

import de.firemage.autograder.core.SerializableCharset;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.compiler.PhysicalFileObject;
import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.compiler.SpoonResource;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSourceInfo implements SourceInfo {
    private static final Logger LOG = LoggerFactory.getLogger(FileSourceInfo.class);

    private final File file;
    private final JavaVersion version;
    private final SerializableCharset charset;

    private FileSourceInfo(Path file, JavaVersion version, Charset charset) {
        this.file = file.toFile();
        this.version = version;
        this.charset = new SerializableCharset(charset);
    }

    public FileSourceInfo(Path file, JavaVersion version) throws IOException {
        if (!file.toFile().isDirectory()) {
            throw new IllegalArgumentException("The file must be a directory");
        }

        this.file = file.toFile();
        this.version = version;

        Charset detectedCharset = null;
        // TODO: make this based on the file
        for (File javaFile : this.streamFiles().toList()) {
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

    @Override
    public Path path() {
        return this.file.toPath();
    }

    private Stream<File> streamFiles() throws IOException {
        return Files.walk(this.file.toPath())
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> !p.toString().endsWith("package-info.java"))
            .map(Path::toFile);
    }

    @Override
    public List<CompilationUnit> compilationUnits() throws IOException {
        return this.streamFiles()
            .map(file -> {
                Path root = this.path();
                Path relative = root.relativize(file.toPath());
                return new PhysicalFileObject(file, this.charset, SourcePath.of(relative));
            })
            .collect(Collectors.toList()); // toList does not work here
    }

    @Override
    public SourceInfo copyTo(Path target) throws IOException {
        FileUtils.copyDirectory(this.file, target.toFile());

        return new FileSourceInfo(target, this.version, this.charset);
    }

    @Override
    public SpoonResource getSpoonResource() {
        return new FileSystemFolder(this.file);
    }

    @Override
    public JavaVersion getVersion() {
        return version;
    }
}
