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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSourceInfo implements SourceInfo {
    private static final Logger LOG = LoggerFactory.getLogger(FileSourceInfo.class);

    private final File file;
    private final JavaVersion version;
    private final Map<SourcePath, SerializableCharset> charsetCache;

    FileSourceInfo(Path path, JavaVersion version) {
        if (!path.toFile().isDirectory()) {
            throw new IllegalArgumentException("The file must be a directory");
        }

        this.file = path.toAbsolutePath().normalize().toFile();
        this.version = version;
        this.charsetCache = new HashMap<>();
    }

    private SerializableCharset detectCharset(File file, SourcePath sourcePath) {
        return this.charsetCache.computeIfAbsent(sourcePath, key -> {
            try {
                return new SerializableCharset(Optional.ofNullable(UniversalDetector.detectCharset(file))
                    .map(Charset::forName)
                    .orElse(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read file '%s' for detecting charset".formatted(sourcePath), e);
            }
        });
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
                SourcePath sourcePath = SourcePath.of(relative);
                SerializableCharset charset = this.detectCharset(file, sourcePath);
                return new PhysicalFileObject(file, charset, sourcePath);
            })
            .collect(Collectors.toList()); // toList does not work here
    }

    @Override
    public SourceInfo copyTo(Path target) throws IOException {
        FileUtils.copyDirectory(this.file, target.toFile());

        return new FileSourceInfo(target, this.version);
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
