package de.firemage.codelinter.cmd.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public class CheckConfig {
    @JsonValue
    private List<CheckFactory> checks;

    @JsonCreator
    public CheckConfig(List<CheckFactory> checks) {
        this.checks = checks;
    }

    public List<CheckFactory> getChecks() {
        return checks;
    }
}
