package de.firemage.autograder.core.pmd;

import de.firemage.autograder.core.Problem;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import org.apache.commons.io.output.NullWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProblemRenderer extends AbstractIncrementingRenderer {
    private final Path root;
    private final Map<String, PMDCheck> checks;
    private final List<Problem> problems = new ArrayList<>();

    public ProblemRenderer(Map<String, PMDCheck> checks, Path root) {
        super("Custom renderer", "Creates InCodeProblems");
        this.checks = checks;
        this.root = root;
        super.setWriter(new NullWriter());
    }

    @Override
    public void renderFileViolations(Iterator<RuleViolation> violations) {
        violations.forEachRemaining(violation -> {
            // NOTE: the caller of this method catches all exceptions, so if something crashes, it will not be
            //       visible without that printStackTrace
            try {
                this.problems.add(new PMDInCodeProblem(this.checks.get(violation.getRule().getName()), violation, root));
            } catch (Exception exception) {
                exception.printStackTrace();
                // make sure the program stops running
                System.exit(-1);
            }
        });
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
