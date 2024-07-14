package de.firemage.autograder.extra.pmd;

import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.CodeLinter;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.CompilationUnit;
import de.firemage.autograder.api.AbstractTempLocation;
import de.firemage.autograder.core.file.UploadedFile;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.document.FileCollector;
import net.sourceforge.pmd.lang.document.FileId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PMDLinter implements CodeLinter<PMDCheck> {
    private static final Language JAVA_LANGUAGE = LanguageRegistry.PMD.getLanguageById("java");

    @Override
    public Class<PMDCheck> supportedCheckType() {
        return PMDCheck.class;
    }

    @Override
    public List<Problem> lint(
        UploadedFile submission,
        AbstractTempLocation tempLocation,
        ClassLoader classLoader,
        List<PMDCheck> checks,
        Consumer<Translatable> statusConsumer
    ) throws IOException {
        statusConsumer.accept(LinterStatus.RUNNING_PMD.getMessage());

        PMDConfiguration config = new PMDConfiguration();

        config.setMinimumPriority(RulePriority.LOW);
        config.setIgnoreIncrementalAnalysis(true);
        config.setClassLoader(classLoader);
        config.setDefaultLanguageVersion(JAVA_LANGUAGE.getVersion(submission.getSource().getVersion().getVersionString()));

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

        ProblemRenderer renderer = new ProblemRenderer(idMap, submission.getSource());

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            pmd.addRuleSet(RuleSet.create("Autograder Configuration (Generated)", "", null, List.of(), List.of(), rules));
            pmd.addRenderer(renderer);
            FileCollector collector = pmd.files();
            for (CompilationUnit compilationUnit : submission.getSource().compilationUnits()) {
                collector.addSourceFile(
                    FileId.fromPathLikeString(submission.getSource().path().resolve(compilationUnit.path().toPath()).toString()),
                    compilationUnit.readString()
                );
            }

            pmd.performAnalysis();
        }

        return renderer.getProblems();
    }
}
