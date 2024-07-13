package de.firemage.autograder.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.firemage.autograder.api.loader.ImplementationBinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record CheckConfiguration(List<? extends AbstractProblemType> problemsToReport, List<String> excludedClasses) {
    public static CheckConfiguration empty() {
        return new CheckConfiguration(List.of(), List.of());
    }

    public static CheckConfiguration fromConfigFile(Path configFile) throws IOException, LinterConfigurationException {
        return CheckConfiguration.fromConfigString(Files.readString(configFile));
    }

    public static CheckConfiguration fromConfigString(String configString) throws IOException, LinterConfigurationException {
        if (!configString.contains("problemsToReport") && configString.startsWith("[")) {
            configString = "problemsToReport: " + configString;
        }

        // Tell Jackson how to instantiate AbstractProblemType
        var coreModule = new SimpleModule("autograder-core");
        coreModule.addAbstractTypeMapping(AbstractProblemType.class, new ImplementationBinder<>(AbstractProblemType.class).findImplementation());

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(coreModule);
        var config =  mapper.readValue(configString, CheckConfiguration.class);
        config.validate();
        return config;
    }

    public static CheckConfiguration fromProblemTypes(List<? extends AbstractProblemType> problemsToReport) {
        return new CheckConfiguration(problemsToReport, List.of());
    }

    private void validate() throws LinterConfigurationException {
        if (this.excludedClasses != null) {
            for (String excludedClass : this.excludedClasses) {
                if (excludedClass.contains("/") || excludedClass.contains(".")) {
                    throw new LinterConfigurationException("Invalid excluded class name '%s'. Please check your configuration.".formatted(excludedClass));
                }
            }
        }
    }
}
