package de.firemage.codelinter.core.file;

import de.firemage.codelinter.core.compiler.JavaVersion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import org.apache.commons.io.FileUtils;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UploadedFile {

    @Getter
    private final Path file;

    @Getter
    private final JavaVersion version;

    public UploadedFile(Path file, JavaVersion version) {
        if (!file.toFile().isDirectory()) {
            throw new IllegalArgumentException("The file must be a directory");
        }

        this.file = file;
        this.version = version;
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

    public void delete() throws IOException {
        FileUtils.deleteDirectory(this.file.toFile());
    }
}
