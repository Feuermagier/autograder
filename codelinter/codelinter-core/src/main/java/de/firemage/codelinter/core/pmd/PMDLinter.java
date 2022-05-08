package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.file.UploadedFile;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PMDLinter {

    public List<Problem> lint(UploadedFile file, List<PMDCheck> checks) throws IOException {
        PMDConfiguration config = new PMDConfiguration();

        config.setMinimumPriority(RulePriority.LOW);
        config.setIgnoreIncrementalAnalysis(true);
        config.setReportShortNames(true);
        config.setDefaultLanguageVersion(LanguageRegistry.findLanguageByTerseName("java").getVersion(file.getVersion().getVersionString()));

        Map<Class<? extends Rule>, Check> idMap = new HashMap<>();
        List<Rule> rules = new ArrayList<>();

        for (PMDCheck check : checks) {
            for (Rule rule : check.getRules()) {
                idMap.put(rule.getClass(), check);
                rules.add(rule);
            }
        }

        ProblemRenderer renderer = new ProblemRenderer(idMap);

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            pmd.addRuleSet(RuleSet.create("Codelinter Configuration (Generated)", "", null, List.of(), List.of(), rules));
            pmd.addRenderer(renderer);
            pmd.files().addDirectory(file.getFile());
            pmd.performAnalysis();
        }

        return renderer.getProblems();
    }
}
