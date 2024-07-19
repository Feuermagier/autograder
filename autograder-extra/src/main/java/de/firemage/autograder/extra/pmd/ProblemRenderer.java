package de.firemage.autograder.extra.pmd;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.FileSourceInfo;
import de.firemage.autograder.core.file.SourceInfo;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import org.apache.commons.io.output.NullWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProblemRenderer extends AbstractIncrementingRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(FileSourceInfo.class);

    private final SourceInfo sourceInfo;
    private final Map<String, PMDCheck> checks;
    private final List<Problem> problems = new ArrayList<>();

    public ProblemRenderer(Map<String, PMDCheck> checks, SourceInfo sourceInfo) {
        super("Custom renderer", "Creates InCodeProblems");
        this.checks = checks;
        this.sourceInfo = sourceInfo;
        super.setWriter(NullWriter.INSTANCE);
    }

    @Override
    public void renderFileViolations(Iterator<RuleViolation> violations) {
        violations.forEachRemaining(violation -> {
            // NOTE: the caller of this method catches all exceptions, so if something crashes, it will not be
            //       visible without that printStackTrace
            try {
                this.problems.add(new PMDInCodeProblem(this.checks.get(violation.getRule().getName()), violation, sourceInfo));
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
            LOG.error("PMD config error: " + error.issue());
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
