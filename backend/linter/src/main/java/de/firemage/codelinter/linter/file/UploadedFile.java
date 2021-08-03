package de.firemage.codelinter.linter.file;

import com.github.javaparser.utils.SourceZip;
import de.firemage.codelinter.linter.compiler.Compiler;
import de.firemage.codelinter.linter.compiler.CompilerResult;
import lombok.Getter;
import net.sourceforge.pmd.util.datasource.ZipDataSource;
import spoon.support.compiler.ZipFolder;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UploadedFile implements AutoCloseable {

    @Getter
    private final File file;

    @Getter
    private File jar;

    @Getter
    private String compilerErrorOutput;

    private final ZipFile zipFile;

    public UploadedFile(File file) throws ZipFormatException, IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file must be a real file and not a folder");
        }

        this.file = file;

        try {
            this.zipFile = new ZipFile(file);
        } catch (ZipException e) {
            throw new ZipFormatException("Couldn't read the zip file", e);
        }
    }

    public void compile() throws IOException {
        CompilerResult compilerResult = Compiler.createJar(file);
        this.jar = compilerResult.jar();
        this.compilerErrorOutput = compilerResult.errorOutput();
    }

    public ZipFolder getSpoonFile() {
        try {
            return new ZipFolder(this.file);
        } catch (IOException e) {
            // This cannot happen because we check all necessary conditions in the constructor
            throw new IllegalStateException("Spoon wasn't able convert the zip file to a zip folder", e);
        }
    }

    public Collection<ZipDataSource> getPMDFiles() {
        List<ZipEntry> entries = new ArrayList<>();
        Enumeration<? extends ZipEntry> entryIterator = this.zipFile.entries();
        while (entryIterator.hasMoreElements()) {
            entries.add(entryIterator.nextElement());
        }
        return entries.stream().map(entry -> new ZipDataSource(zipFile, entry)).collect(Collectors.toList());
    }

    public SourceZip getJavaParserFile() {
        return new SourceZip(this.file.toPath());
    }

    @Override
    public void close() throws IOException {
        this.zipFile.close();
    }

    public void delete() {
        this.file.delete();
        this.jar.delete();
    }
}
