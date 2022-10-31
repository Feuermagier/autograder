package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.check.general.CopyPasteCheck;
import de.firemage.autograder.core.compiler.CompilationResult;
import de.firemage.autograder.core.compiler.Compiler;
import de.firemage.autograder.core.cpd.CPDLinter;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.IntegratedAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import de.firemage.autograder.core.pmd.PMDLinter;
import de.firemage.autograder.core.spotbugs.SpotbugsCheck;
import de.firemage.autograder.core.spotbugs.SpotbugsLinter;
import fluent.bundle.FluentBundle;
import fluent.bundle.FluentResource;
import fluent.functions.icu.ICUFunctionFactory;
import fluent.syntax.parser.FTLParser;
import fluent.syntax.parser.FTLStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class Linter {
    private final FluentBundle fluentBundle;

    public Linter(Locale locale) {
        try {
            FluentResource resource = FTLParser.parse(
                FTLStream.of(Files.readString(Path.of(this.getClass().getResource("/strings.en.ftl").toURI()))));
            this.fluentBundle = FluentBundle.builder(locale, ICUFunctionFactory.INSTANCE).addResource(resource).build();
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Problem> checkFile(UploadedFile file, Path tmpLocation, Path tests, List<Check> checks,
                                   Consumer<LinterStatus> statusConsumer, boolean disableDynamicAnalysis)
        throws LinterException, InterruptedException, IOException {
        statusConsumer.accept(LinterStatus.COMPILING);
        CompilationResult result = Compiler.compileToJar(file, tmpLocation, file.getVersion());

        List<PMDCheck> pmdChecks = new ArrayList<>();
        List<SpotbugsCheck> spotbugsChecks = new ArrayList<>();
        List<CopyPasteCheck> cpdChecks = new ArrayList<>();
        List<IntegratedCheck> integratedChecks = new ArrayList<>();

        for (Check check : checks) {
            if (check instanceof PMDCheck pmdCheck) {
                pmdChecks.add(pmdCheck);
            } else if (check instanceof CopyPasteCheck cpdCheck) {
                cpdChecks.add(cpdCheck);
            } else if (check instanceof SpotbugsCheck spotbugsCheck) {
                spotbugsChecks.add(spotbugsCheck);
            } else if (check instanceof IntegratedCheck integratedCheck) {
                integratedChecks.add(integratedCheck);
            } else {
                throw new IllegalStateException();
            }
        }

        List<Problem> problems = new ArrayList<>();

        if (!pmdChecks.isEmpty()) {
            statusConsumer.accept(LinterStatus.RUNNING_PMD);
            problems.addAll(new PMDLinter().lint(file, pmdChecks));
        }

        if (!cpdChecks.isEmpty()) {
            statusConsumer.accept(LinterStatus.RUNNING_CPD);
            problems.addAll(new CPDLinter().lint(file, cpdChecks));
        }

        if (!spotbugsChecks.isEmpty()) {
            statusConsumer.accept(LinterStatus.RUNNING_SPOTBUGS);
            problems.addAll(new SpotbugsLinter().lint(result.jar(), spotbugsChecks));
        }

        if (!integratedChecks.isEmpty()) {
            try (
                IntegratedAnalysis analysis = new IntegratedAnalysis(file, result.jar(), tmpLocation, statusConsumer)) {
                if (!disableDynamicAnalysis) {
                    analysis.runDynamicAnalysis(tests, statusConsumer);
                }
                problems.addAll(analysis.lint(integratedChecks, statusConsumer));
            }
        }


        result.jar().toFile().delete();
        return problems;
    }

    public String translateMessage(LocalizedMessage message) {
        return message.format(this.fluentBundle);
    }
}
