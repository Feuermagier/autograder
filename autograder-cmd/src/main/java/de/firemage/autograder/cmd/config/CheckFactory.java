package de.firemage.autograder.cmd.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.firemage.autograder.core.check.Check;

import java.util.List;

@JsonDeserialize(using = CheckDeserializer.class)
public interface CheckFactory {
    List<Check> create();
}
