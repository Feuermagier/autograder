package de.firemage.codelinter.cmd.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.firemage.codelinter.core.check.Check;

import java.util.List;

@JsonDeserialize(using = CheckDeserializer.class)
public interface CheckFactory {
    List<Check> create();
}
