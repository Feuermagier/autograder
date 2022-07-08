package de.firemage.autograder.core.integrated.modelmatching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.modelmatching.expected.ExpectedModel;
import de.firemage.autograder.core.integrated.modelmatching.expected.ExpectedType;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ModelMatcher {
    public void match(StaticAnalysis analysis, Path modelFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        ExpectedModel expectedModel = mapper.readValue(modelFile.toFile(), ExpectedModel.class);
        
        analysis.getSpoonModel().processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> type) {
                for (Map.Entry<String, ExpectedType> expectedType : expectedModel.getTypes().entrySet()) {
                    if (nameMatches(type.getSimpleName(), expectedType.getValue().getNames())) {
                        System.out.println("Match: " + type.getQualifiedName());
                    }
                }
            }
        });
    }
    
    private boolean nameMatches(String name, List<String> options) {
        name = name.toLowerCase();
        for (String option : options) {
            option = option.toLowerCase();
            if (((name.length() >= 3) && option.contains(name)) || option.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
