package de.firemage.codelinter.linter.spoon;

import lombok.Getter;
import spoon.reflect.declaration.CtElement;

public class InCodeProblem implements Problem {
    @Getter
    private final String displayPath;

    @Getter
    private final String filePath;

    @Getter
    private final int line;

    @Getter
    private final int column;

    @Getter
    private final String description;

    @Getter
    private final ProblemCategory category;

    @Getter
    private final String explanation;

    public InCodeProblem(CtElement element, String description, ProblemCategory category, String explanation) {
        this.displayPath = FormatUtil.formatPath(element);
        this.filePath = element.getPosition().getFile().getPath();
        this.line = element.getPosition().getLine();
        this.column = element.getPosition().getColumn();
        this.description = description;
        this.category = category;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "InCodeProblem at " + filePath + ":" + line + ". " + description;
    }
}
