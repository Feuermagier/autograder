package de.firemage.codelinter.linter.pmd;

import de.firemage.codelinter.linter.Problem;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ProblemRenderer extends AbstractIncrementingRenderer {
    private final List<Problem> problems;

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

    @Override
    public void end() throws IOException {
        //TODO Don't ignore processing errors (via Report.ProcessingError)

        for (Report.ConfigurationError error : configErrors) {
            log.error("PMD config error: " + error.issue());
        }
    }

    @Override
    public void start() {
        // Do nothing for this renderer
    }

    @Override
    public void flush() {
        // Do nothing for this renderer
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }
}
