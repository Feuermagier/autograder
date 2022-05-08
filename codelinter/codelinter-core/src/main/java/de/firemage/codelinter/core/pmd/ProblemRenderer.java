package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.Problem;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import org.apache.commons.io.output.NullWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProblemRenderer extends AbstractIncrementingRenderer {
    private final Map<Class<? extends Rule>, Check> checks;
    private final List<Problem> problems = new ArrayList<>();

    public ProblemRenderer(Map<Class<? extends Rule>, Check> checks) {
        super("Custom renderer", "Creates InCodeProblems");
        this.checks = checks;
        super.setWriter(new NullWriter());
    }

    @Override
    public void renderFileViolations(Iterator<RuleViolation> violations) {
        violations.forEachRemaining(violation ->
                problems.add(new PMDInCodeProblem(this.checks.get(violation.getRule().getClass()), violation)));
    }

    @Override
    public String defaultFileExtension() {
        return null;
    }

    @Override
    public void end() {
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
