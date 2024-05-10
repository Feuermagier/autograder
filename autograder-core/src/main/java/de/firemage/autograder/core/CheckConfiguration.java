package de.firemage.autograder.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record CheckConfiguration(List<ProblemType> problemsToReport, List<String> excludedClasses) {
    public static CheckConfiguration fromConfigFile(Path configFile) throws IOException {
        return CheckConfiguration.fromConfigString(Files.readString(configFile));
    }

    public static CheckConfiguration fromConfigString(String configString) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(configString, CheckConfiguration.class);
    }
}
