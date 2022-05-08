package de.firemage.codelinter.core.spoon;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import lombok.Getter;
import java.util.function.Supplier;

public abstract class SpoonCheck implements Check {
    @Getter
    private final String description;

    protected SpoonCheck(String description) {
        this.description = description;
    }

    public abstract Supplier<? extends CodeProcessor> getProcessor();
}
