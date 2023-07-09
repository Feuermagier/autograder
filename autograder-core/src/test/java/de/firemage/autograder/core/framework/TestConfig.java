package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.check.Check;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record TestConfig(List<String> lines) {
    public static TestConfig fromPath(Path path) {
        try {
            List<String> lines = Files.readAllLines(path.resolve("config.txt"));

            if (lines.size() < 2) {
                throw new IllegalArgumentException("Config file must contain at least two lines");
            }

            return new TestConfig(lines);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String checkPath() {
        return this.lines.get(0);
    }

    public String description() {
        return this.lines.get(1);
    }

    public String qualifiedName() {
        return "de.firemage.autograder.core.check." + this.checkPath();
    }

    public Check check() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return (Check) Class.forName(this.qualifiedName())
                .getDeclaredConstructor()
                .newInstance();
    }
}
