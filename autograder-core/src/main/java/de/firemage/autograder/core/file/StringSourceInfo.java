package de.firemage.autograder.core.file;

import de.firemage.autograder.core.compiler.JavaVersion;
import org.apache.commons.io.FileUtils;
import spoon.compiler.SpoonResource;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.VirtualFolder;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class StringSourceInfo implements SourceInfo {
    private static final String VIRTUAL_FOLDER = "virtualSrc";
    private final List<VirtualFileObject> compilationUnits;
    private final JavaVersion version;

    private StringSourceInfo(JavaVersion version, List<VirtualFileObject> compilationUnits) {
        this.compilationUnits = compilationUnits;
        this.version = version;
    }

    public static SourceInfo fromSourceString(JavaVersion version, String className, String source) {
        return StringSourceInfo.fromSourceStrings(version, Map.of(className, source));
    }

    public static SourceInfo fromSourceStrings(JavaVersion version, Map<String, String> sources) {
        List<VirtualFileObject> compilationUnits = new ArrayList<>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            compilationUnits.add(new VirtualFileObject(ClassPath.fromString(entry.getKey()), entry.getValue()));
        }

        return new StringSourceInfo(version, compilationUnits);
    }

    @Override
    public List<CompilationUnit> compilationUnits() {
        return new ArrayList<>(this.compilationUnits);
    }

    @Override
    public SourceInfo copyTo(Path target) throws IOException {
        for (CompilationUnit file : this.compilationUnits()) {
            Path targetFile = target.resolve(Path.of(file.path().toString()));
            FileUtils.createParentDirectories(targetFile.toFile());

            Files.writeString(targetFile, file.readString(), file.charset());
        }

        return new FileSourceInfo(target, this.version);
    }

    @Override
    public Path path() {
        return Path.of(VIRTUAL_FOLDER);
    }

    @Override
    public SpoonResource getSpoonResource() {
        VirtualFolder result = new VirtualFolder();

        for (VirtualFileObject file : this.compilationUnits) {
            result.addFile(new VirtualFile(file.getCode(), SourcePath.of(this.path()).resolve(file.path()).toString()));
        }

        return result;
    }

    @Override
    public JavaVersion getVersion() {
        return this.version;
    }

    // TODO: this class is not serializable...
    private static final class VirtualFileObject extends SimpleJavaFileObject implements Serializable, CompilationUnit {
        private final ClassPath classPath;
        private final String code;

        private VirtualFileObject(ClassPath classPath, String code) {
            super(virtualUri(classPath), Kind.SOURCE);
            this.classPath = classPath;
            this.code = code;
        }

        private static URI virtualUri(ClassPath classPath) {
            return URI.create("string:///%s/%s%s".formatted(
                VIRTUAL_FOLDER,
                classPath.toString().replace('.', '/'),
                JavaFileObject.Kind.SOURCE.extension
            ));
        }

        @Override
        public String getName() {
            return this.path().toString();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return this.code;
        }

        public String getCode() {
            return this.code;
        }

        @Override
        public JavaFileObject toJavaFileObject() {
            return this;
        }

        @Override
        public SourcePath path() {
            return this.classPath.toPath();
        }

        @Override
        public Charset charset() {
            // virtual files are always UTF-8
            return StandardCharsets.UTF_8;
        }
    }

    private record ClassPath(List<String> path, String name) implements Serializable {
        private static ClassPath fromString(String string) {
            if (string.startsWith(".")) {
                throw new IllegalArgumentException("Class path must not start with a dot: '%s'".formatted(string));
            }
            List<String> parts = Arrays.asList(string.split("\\.", -1));
            return new ClassPath(
                new ArrayList<>(parts.subList(0, parts.size() - 1)),
                parts.get(parts.size() - 1)
            );
        }

        private List<String> segments() {
            List<String> result = new ArrayList<>(this.path);
            result.add(this.name);
            return result;
        }

        @Override
        public String toString() {
            return String.join(".", this.segments());
        }

        SourcePath toPath() {
            List<String> segments = new ArrayList<>(this.path);
            segments.add(this.name + JavaFileObject.Kind.SOURCE.extension);
            return SourcePath.of(segments);
        }
    }
}
