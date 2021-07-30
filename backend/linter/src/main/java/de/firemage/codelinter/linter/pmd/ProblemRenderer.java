package de.firemage.codelinter.linter.pmd;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProblemRenderer extends AbstractIncrementingRenderer {
    private final List<InCodeProblem> problems;

    public ProblemRenderer() {
        super("Custom renderer", "Creates InCodeProblems");
        problems = new ArrayList<>();
    }

    @Override
    public void renderFileViolations(Iterator<RuleViolation> violations) {
        violations.forEachRemaining(violation -> problems.add(new PMDInCodeProblem(violation)));
    }

    @Override
    public String defaultFileExtension() {
        return null;
    }

    public List<InCodeProblem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }
}
