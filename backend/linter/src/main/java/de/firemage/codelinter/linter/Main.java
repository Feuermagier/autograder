package de.firemage.codelinter.linter;

import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.processor.AssertProcessor;
import de.firemage.codelinter.linter.spoon.processor.CatchProcessor;
import de.firemage.codelinter.linter.spoon.processor.IllegalExitProcessor;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import net.sourceforge.pmd.util.datasource.ZipDataSource;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;
import spoon.reflect.CtModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    public static void main(String[] args) throws IOException {

        Launcher launcher = new Launcher();
        //launcher.addInputResource(new ZipFolder(new File("A1.zip")));
        launcher.addInputResource("Test.java");
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setComplianceLevel(11);
        CtModel model = null;
        try {
            model = launcher.buildModel();
        } catch (ModelBuildingException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        ProblemLogger logger = new ProblemLogger();

        CatchProcessor catchProcessor = new CatchProcessor(logger);
        model.processWith(catchProcessor);

        IllegalExitProcessor exitProcessor = new IllegalExitProcessor(logger);
        model.processWith(exitProcessor);

        AssertProcessor assertProcessor = new AssertProcessor(logger);
        model.processWith(assertProcessor);

        System.out.println(logger.getProblems().stream().map(Object::toString).collect(Collectors.joining(",")));


        //runPMD(new ZipFile("A1.zip"));
    }

    public static void runPMD(ZipFile zipFile) throws IOException {
        PMDConfiguration config = new PMDConfiguration();
        config.setRuleSets("pmd/ruleset.xml");
        config.setMinimumPriority(RulePriority.MEDIUM);
        config.setIgnoreIncrementalAnalysis(true);

        RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(config);
        List<RuleSet> ruleSets = ruleSetLoader.loadFromResources(Arrays.asList(config.getRuleSets().split(",")));

        List<ZipEntry> entries = new ArrayList<>();
        Enumeration<? extends ZipEntry> entryIterator = zipFile.entries();
        while (entryIterator.hasMoreElements()) {
            entries.add(entryIterator.nextElement());
        }
        List<ZipDataSource> files = entries.stream().map(entry -> new ZipDataSource(zipFile, entry)).collect(Collectors.toList());
        Renderer renderer = new XMLRenderer();
        StringWriter output = new StringWriter();
        renderer.setWriter(output);
        renderer.start();
        PMD.processFiles(config, ruleSets, files, Collections.singletonList(renderer));
        renderer.end();
        renderer.flush();
        System.out.println(output);
    }
}
