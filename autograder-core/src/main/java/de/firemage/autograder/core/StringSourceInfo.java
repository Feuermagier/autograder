package de.firemage.autograder.core;

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
    private final List<StringSource> compilationUnits;
    private final JavaVersion version;

    private StringSourceInfo(JavaVersion version, List<StringSource> compilationUnits) {
        this.compilationUnits = compilationUnits;
        this.version = version;
    }

    public static SourceInfo fromSourceString(JavaVersion version, String className, String source) {
        return StringSourceInfo.fromSourceStrings(version, Map.of(className, source));
    }

    public static SourceInfo fromSourceStrings(JavaVersion version, Map<String, String> sources) {
        List<StringSource> compilationUnits = new ArrayList<>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            compilationUnits.add(new StringSource(ClassPath.fromString(entry.getKey()), entry.getValue()));
        }

        return new StringSourceInfo(version, compilationUnits);
    }

    @Override
    public Path getPath() {
        return Path.of("stringSrc");
    }

    @Override
    public List<JavaFileObject> compilationUnits() {
        return this.compilationUnits.stream().map(source -> (JavaFileObject) new VirtualFileObject(source.classPath, source.code)).toList();
    }

    @Override
    public SourceInfo copyTo(Path target) throws IOException {
        Charset charset = this.getCharset();
        for (JavaFileObject file : this.compilationUnits()) {
            Path targetFile = target.resolve(file.getName());
            FileUtils.createParentDirectories(targetFile.toFile());

            Files.writeString(targetFile, file.getCharContent(false), charset);
        }

        return new FileSourceInfo(target, this.version);
    }

    @Override
    public SpoonResource getSpoonResource() {
        VirtualFolder result = new VirtualFolder();

        for (StringSource file : this.compilationUnits) {
            result.addFile(new VirtualFile(file.code, file.classPath.name()));
        }

        return result;
    }

    @Override
    public void delete() {
        // nothing to delete (source code is in memory)
    }

    @Override
    public JavaVersion getVersion() {
        return this.version;
    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    // This class exists only to make StringSourceInfo serializable because VirtualFileObject isn't serializable
    private record StringSource(ClassPath classPath, String code) implements Serializable {

    }

    private static final class VirtualFileObject extends SimpleJavaFileObject {
        private final ClassPath classPath;
        private final String code;

        private VirtualFileObject(ClassPath classPath, String code) {
            super(classPath.toUri(), Kind.SOURCE);
            this.classPath = classPath;
            this.code = code;
        }

        @Override
        public String getName() {
            return this.classPath.toPath() + JavaFileObject.Kind.SOURCE.extension;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return this.code;
        }
    }

    private record ClassPath(String path, String name) implements Serializable {
        public static ClassPath fromString(String string) {
            List<String> parts = Arrays.asList(string.split("\\.", -1));
            return new ClassPath(
                    String.join(".", parts.subList(0, parts.size() - 1)),
                    parts.get(parts.size() - 1)
            );
        }

        @Override
        public String toString() {
            if (this.path.isBlank()) {
                return this.name;
            } else {
                return this.path + "." + this.name;
            }
        }

        public Path toPath() {
            return Path.of(this.path.replace('.', '/'), this.name);
        }

        public URI toUri() {
            return URI.create("string:///" + this.toString().replace('.', '/') + JavaFileObject.Kind.SOURCE.extension);
        }
    }
}
