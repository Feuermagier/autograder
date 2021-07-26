package de.firemage.codelinter.main.result;

import de.firemage.codelinter.linter.pmd.PMDRuleset;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("pmd")
public class PMDConfig {

    @Setter
    private String ruleset = "default.xml";

    public PMDRuleset getRuleset() {
        return new PMDRuleset(this.ruleset);
    }

}
