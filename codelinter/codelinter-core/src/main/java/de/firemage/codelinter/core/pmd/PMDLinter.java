package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.file.UploadedFile;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.JavaLanguage;
import net.sourceforge.pmd.cpd.Language;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PMDLinter {

    public List<Problem> lint(UploadedFile file, PMDRuleset ruleset) throws IOException {
        PMDConfiguration config = new PMDConfiguration();
        config.setRuleSets(ruleset.path());
        config.setMinimumPriority(RulePriority.LOW);
        config.setIgnoreIncrementalAnalysis(true);
        config.setReportShortNames(true);

        RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(config);
        List<RuleSet> ruleSets = ruleSetLoader.loadFromResources(Arrays.asList(config.getRuleSets().split(",")));

        ProblemRenderer renderer = new ProblemRenderer(file.getFile());
        renderer.start();
        PMD.processFiles(config, ruleSets, file.getPMDFiles(), Collections.singletonList(renderer));
        renderer.end();
        renderer.flush();

        return renderer.getProblems();
    }
}
