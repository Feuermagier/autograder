package de.firemage.codelinter.web.result;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties("rules")
public class RuleConfig {

    @Setter
    private String pmd = "default.xml";

    public Path getPMDRuleset() {
        return Paths.get(this.pmd);
    }
}
