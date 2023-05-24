package de.firemage.autograder.core.pmd;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.UploadedFile;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.document.FileCollector;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PMDLinter {
    private static final Language JAVA_LANGUAGE = LanguageRegistry.PMD.getLanguageById("java");

    public List<Problem> lint(UploadedFile file, List<PMDCheck> checks, ClassLoader classLoader) throws IOException {
        PMDConfiguration config = new PMDConfiguration();

        Path root = file.getSource().getPath();
        config.setMinimumPriority(RulePriority.LOW);
        config.setIgnoreIncrementalAnalysis(true);
        config.addRelativizeRoot(root);
        config.setClassLoader(classLoader);
        config.setDefaultLanguageVersion(JAVA_LANGUAGE.getVersion(file.getSource().getVersion().getVersionString()));

        Map<String, PMDCheck> idMap = new HashMap<>();
        List<Rule> rules = new ArrayList<>();

        int idCounter = 0;
        for (PMDCheck check : checks) {
            for (Rule rule : check.getRules()) {
                String id = String.valueOf(idCounter++);
                rule.setName(id);
                idMap.put(id, check);
                rules.add(rule);
            }
        }

        ProblemRenderer renderer = new ProblemRenderer(idMap, root);

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            pmd.addRuleSet(RuleSet.create("Autograder Configuration (Generated)", "", null, List.of(), List.of(), rules));
            pmd.addRenderer(renderer);
            FileCollector collector = pmd.files();
            for (JavaFileObject compilationUnit : file.getSource().compilationUnits()) {
                collector.addSourceFile(
                    compilationUnit.getCharContent(false).toString(),
                    compilationUnit.getName()
                );
            }

            pmd.performAnalysis();
        }

        return renderer.getProblems();
    }
}
