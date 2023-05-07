package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.general.CopyPasteCheck;
import de.firemage.autograder.core.cpd.CPDLinter;
import de.firemage.autograder.core.errorprone.ErrorProneCheck;
import de.firemage.autograder.core.errorprone.ErrorProneLinter;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.IntegratedAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.parallel.AnalysisResult;
import de.firemage.autograder.core.parallel.AnalysisScheduler;
import de.firemage.autograder.core.pmd.PMDCheck;
import de.firemage.autograder.core.pmd.PMDLinter;
import de.firemage.autograder.core.spotbugs.SpotbugsCheck;
import de.firemage.autograder.core.spotbugs.SpotbugsLinter;
import fluent.bundle.FluentBundle;
import fluent.bundle.FluentResource;
import fluent.functions.icu.ICUFunctionFactory;
import fluent.syntax.parser.FTLParser;
import fluent.syntax.parser.FTLStream;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class Linter {
    private final FluentBundle fluentBundle;

    public Linter(Locale locale) {
        String filename = switch (locale.getLanguage()) {
            case "de" -> "/strings.de.ftl";
            case "en" -> "/strings.en.ftl";
            default -> throw new IllegalArgumentException("No translation available for the locale " + locale);
        };
        try {
            FluentResource resource = FTLParser.parse(FTLStream.of(
                    new String(this.getClass().getResourceAsStream(filename).readAllBytes(), StandardCharsets.UTF_8)
            ));
            this.fluentBundle = FluentBundle.builder(locale, ICUFunctionFactory.INSTANCE).addResource(resource).build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public FluentBundle getFluentBundle() {
        return fluentBundle;
    }

    public List<Problem> checkFile(UploadedFile file, Path tmpLocation, Path tests,
                                   List<ProblemType> problemsToReport,
                                   Consumer<LinterStatus> statusConsumer, boolean disableDynamicAnalysis,
                                   int threads)
            throws LinterException, IOException, InterruptedException {
        return this.checkFile(file, tmpLocation, tests, problemsToReport,
                findChecksForProblemTypes(problemsToReport),
                statusConsumer, disableDynamicAnalysis, threads);
    }

    public List<Problem> checkFile(UploadedFile file, Path tmpLocation, Path tests,
                                   List<ProblemType> problemsToReport,
                                   List<Check> checks, Consumer<LinterStatus> statusConsumer,
                                   boolean disableDynamicAnalysis, int threads) throws LinterException {

        List<PMDCheck> pmdChecks = new ArrayList<>();
        List<SpotbugsCheck> spotbugsChecks = new ArrayList<>();
        List<CopyPasteCheck> cpdChecks = new ArrayList<>();
        List<IntegratedCheck> integratedChecks = new ArrayList<>();
        List<ErrorProneCheck> errorProneChecks = new ArrayList<>();

        for (Check check : checks) {
            if (check instanceof PMDCheck pmdCheck) {
                pmdChecks.add(pmdCheck);
            } else if (check instanceof CopyPasteCheck cpdCheck) {
                cpdChecks.add(cpdCheck);
            } else if (check instanceof SpotbugsCheck spotbugsCheck) {
                spotbugsChecks.add(spotbugsCheck);
            } else if (check instanceof IntegratedCheck integratedCheck) {
                integratedChecks.add(integratedCheck);
            } else if (check instanceof ErrorProneCheck errorProneCheck) {
                errorProneChecks.add(errorProneCheck);
            } else {
                throw new IllegalStateException();
            }
        }

        AnalysisScheduler scheduler = new AnalysisScheduler(threads);

        if (!pmdChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                statusConsumer.accept(LinterStatus.RUNNING_PMD);
                reporter.reportProblems(new PMDLinter().lint(file, pmdChecks));
            });
        }

        if (!cpdChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                statusConsumer.accept(LinterStatus.RUNNING_CPD);
                reporter.reportProblems(new CPDLinter().lint(file, cpdChecks));
            });
        }

        if (!spotbugsChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                statusConsumer.accept(LinterStatus.RUNNING_SPOTBUGS);
                reporter.reportProblems(new SpotbugsLinter().lint(file.getCompilationResult().jar(), spotbugsChecks));
            });
        }

        if (!integratedChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                IntegratedAnalysis analysis = new IntegratedAnalysis(file, tmpLocation);
                if (!disableDynamicAnalysis) {
                    analysis.runDynamicAnalysis(tests, statusConsumer);
                }
                analysis.lint(integratedChecks, statusConsumer, s);
            });
        }

        if (!errorProneChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                statusConsumer.accept(LinterStatus.RUNNING_ERROR_PRONE);
                reporter.reportProblems(new ErrorProneLinter().lint(file, errorProneChecks));
            });
        }

        AnalysisResult result = scheduler.collectProblems();
        if (result.failed()) {
            throw new LinterException(result.thrownException());
        }

        if (problemsToReport.isEmpty()) {
            return result.problems();
        } else {
            return result
                    .problems()
                    .stream()
                    .filter(p -> problemsToReport.contains(p.getProblemType()))
                    .toList();
        }
    }

    public String translateMessage(LocalizedMessage message) {
        String output = message.format(this.fluentBundle);
        if (output.startsWith("Unknown messageID '")) {
            return output.substring("Unknown messageID '".length(), output.length() - 1);
        } else {
            return output;
        }
    }

    private List<Check> findChecksForProblemTypes(List<ProblemType> problems) {
        Reflections reflections = new Reflections("de.firemage.autograder.core.check");
        return reflections.getTypesAnnotatedWith(ExecutableCheck.class).stream()
                .filter(c -> isRequiredCheck(c.getAnnotation(ExecutableCheck.class), problems))
                .map(c -> {
                    try {
                        return (Check) c.getConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to instantiate check " + c.getName(), e);
                    } catch (ClassCastException e) {
                        throw new IllegalStateException(c.getName() + " does not inherit from Check");
                    }
                })
                .toList();
    }

    private boolean isRequiredCheck(ExecutableCheck check, List<ProblemType> problems) {
        return check.enabled() && problems.stream().anyMatch(p -> List.of(check.reportedProblems()).contains(p));
    }
}
