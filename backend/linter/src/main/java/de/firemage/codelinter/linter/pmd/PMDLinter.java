package de.firemage.codelinter.linter.pmd;

import de.firemage.codelinter.linter.file.UploadedFile;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.TextRenderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import net.sourceforge.pmd.util.ClasspathClassLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PMDLinter {

    public String lint(UploadedFile file, PMDRuleset ruleset) throws IOException {
        PMDConfiguration config = new PMDConfiguration();
        config.setRuleSets(ruleset.path());
        config.setMinimumPriority(RulePriority.MEDIUM);
        config.setIgnoreIncrementalAnalysis(true);
        config.setReportShortNames(true);

        RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(config);
        List<RuleSet> ruleSets = ruleSetLoader.loadFromResources(Arrays.asList(config.getRuleSets().split(",")));

        Renderer renderer = new NoPathTextRenderer();
        StringWriter output = new StringWriter();
        renderer.setWriter(output);
        renderer.start();
        try {
            PMD.processFiles(config, ruleSets, file.getPMDFiles(), Collections.singletonList(renderer));
        } finally {
            ClassLoader auxiliaryClassLoader = config.getClassLoader();
            if (auxiliaryClassLoader instanceof ClasspathClassLoader classpathClassLoader) {
                // ClasspathClassLoader is deprecated, but not closing it may leak resources
                classpathClassLoader.close();
            }
        }
        renderer.end();
        renderer.flush();

        return output.toString();
    }
}
