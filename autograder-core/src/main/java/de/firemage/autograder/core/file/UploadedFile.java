package de.firemage.autograder.core.file;

import de.firemage.autograder.core.compiler.JavaVersion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class UploadedFile {

    private final Path file;

    @Getter
    private final JavaVersion version;
    
    @Getter
    private final Charset charset;

    public UploadedFile(Path file, JavaVersion version) throws IOException {
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
                    throw new IOException("Java files with incompatible encodings found - some are " + detectedCharset + ", but others are " + fileCharset);
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
}
