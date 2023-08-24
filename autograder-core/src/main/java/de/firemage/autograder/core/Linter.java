package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.general.CopyPasteCheck;
import de.firemage.autograder.core.cpd.CPDLinter;
import de.firemage.autograder.core.errorprone.ErrorProneCheck;
import de.firemage.autograder.core.errorprone.ErrorProneLinter;
import de.firemage.autograder.core.errorprone.TempLocation;
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
import org.reflections.scanners.Scanners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Linter {
    private final int threads;
    private final TempLocation tempLocation;
    private final FluentBundle fluentBundle;
    private final boolean disableDynamicAnalysis;
    private final ClassLoader classLoader;
    private final int maxProblemsPerCheck;

    private Linter(
        Locale locale,
        TempLocation tempLocation,
        int threads,
        boolean disableDynamicAnalysis,
        ClassLoader classLoader,
        int maxProblemsPerCheck
    ) {
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

        this.tempLocation = tempLocation;
        this.threads = threads;
        this.disableDynamicAnalysis = disableDynamicAnalysis;
        this.classLoader = classLoader;
        this.maxProblemsPerCheck = maxProblemsPerCheck;
    }

    public static class Builder {
        private final Locale locale;
        private TempLocation tempLocation;
        private int threads;
        private boolean disableDynamicAnalysis = true;
        private ClassLoader classLoader;
        private int maxProblemsPerCheck = -1;

        private Builder(Locale locale) {
            this.locale = locale;
        }

        public Builder tempLocation(TempLocation tempLocation) {
            this.tempLocation = tempLocation;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder maxProblemsPerCheck(int maxProblemsPerCheck) {
            this.maxProblemsPerCheck = maxProblemsPerCheck;
            return this;
        }

        public Builder enableDynamicAnalysis() {
            return this.enableDynamicAnalysis(true);
        }

        public Builder enableDynamicAnalysis(boolean shouldEnable) {
            this.disableDynamicAnalysis = !shouldEnable;
            return this;
        }

        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Linter build() {
            TempLocation tempLocation = this.tempLocation;

            if (tempLocation == null) {
                tempLocation = TempLocation.random();
            }

            return new Linter(
                this.locale,
                tempLocation,
                this.threads,
                this.disableDynamicAnalysis,
                this.classLoader,
                this.maxProblemsPerCheck
            );
        }
    }

    public static Linter defaultLinter(Locale locale) {
        return Linter.builder(locale).build();
    }

    public static Builder builder(Locale locale) {
        return new Builder(locale);
    }

    public FluentBundle getFluentBundle() {
        return fluentBundle;
    }

    public List<Problem> checkFile(
        UploadedFile file, Path tests,
        List<ProblemType> problemsToReport,
        Consumer<LinterStatus> statusConsumer
    ) throws LinterException, IOException {
        return this.checkFile(
            file,
            tests,
            problemsToReport,
            findChecksForProblemTypes(problemsToReport),
            statusConsumer
        );
    }

    public List<Problem> checkFile(
        UploadedFile file,
        Path tests,
        Collection<ProblemType> problemsToReport,
        Iterable<? extends Check> checks,
        Consumer<LinterStatus> statusConsumer
    ) throws LinterException, IOException {
        // the file is null if the student did not upload source code
        if (file == null) {
            return new ArrayList<>();
        }

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
            }

            // allow checks to implement ErrorProneCheck in addition to extending a parent class
            if (check instanceof ErrorProneCheck errorProneCheck) {
                errorProneChecks.add(errorProneCheck);
            }
        }

        AnalysisScheduler scheduler = new AnalysisScheduler(this.threads, classLoader);

        if (!pmdChecks.isEmpty()) {
            scheduler.submitTask((s, reporter) -> {
                statusConsumer.accept(LinterStatus.RUNNING_PMD);
                reporter.reportProblems(new PMDLinter().lint(file, pmdChecks, this.classLoader));
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
                reporter.reportProblems(new SpotbugsLinter().lint(file, file.getCompilationResult().jar(), spotbugsChecks));
            });
        }

        AnalysisResult result;
        try (TempLocation tempLinterLocation = this.tempLocation.createTempDirectory("linter")) {
            Path tmpLocation = tempLinterLocation.toPath();

            if (!integratedChecks.isEmpty()) {
                scheduler.submitTask((s, reporter) -> {
                    IntegratedAnalysis analysis = new IntegratedAnalysis(file, tmpLocation);
                    if (!this.disableDynamicAnalysis) {
                        analysis.runDynamicAnalysis(tests, statusConsumer);
                    }
                    analysis.lint(integratedChecks, statusConsumer, s);
                });
            }

            if (!errorProneChecks.isEmpty()) {
                scheduler.submitTask((s, reporter) -> {
                    statusConsumer.accept(LinterStatus.RUNNING_ERROR_PRONE);
                    reporter.reportProblems(new ErrorProneLinter().lint(file, tempLinterLocation, errorProneChecks));
                });
            }

            result = scheduler.collectProblems();
            if (result.failed()) {
                throw new LinterException(result.thrownException());
            }
        }

        List<Problem> unreducedProblems = result.problems();
        if (!problemsToReport.isEmpty()) {
            unreducedProblems = result
                .problems()
                .stream()
                .filter(p -> problemsToReport.contains(p.getProblemType()))
                .toList();
        }

        return this.mergeProblems(unreducedProblems);
    }

    private List<Problem> mergeProblems(Collection<? extends Problem> unreducedProblems) {
        // -1 means no limit (useful for unit tests, where one wants to see all problems)
        if (this.maxProblemsPerCheck == -1) {
            return new ArrayList<>(unreducedProblems);
        }

        // first group all problems by the check that created them
        Map<Check, List<Problem>> problems = unreducedProblems.stream()
            .collect(Collectors.groupingBy(Problem::getCheck, LinkedHashMap::new, Collectors.toList()));

        List<Problem> result = new ArrayList<>();
        for (Map.Entry<Check, List<Problem>> entry : problems.entrySet()) {
            Check check = entry.getKey();
            List<Problem> problemsForCheck = entry.getValue();
            // then go through each check and merge the problems if they exceed the maxProblemsPerCheck
            if (problemsForCheck.size() > Math.min(this.maxProblemsPerCheck, entry.getKey().maximumProblems().orElse(this.maxProblemsPerCheck))) {
                // further partition the problems by their ProblemType
                // (one does not want to merge different types of problems):
                Map<ProblemType, List<Problem>> problemsByType = problemsForCheck.stream()
                    .collect(Collectors.groupingBy(Problem::getProblemType, LinkedHashMap::new, Collectors.toList()));

                problemsForCheck = problemsByType.values()
                    .stream()
                    .flatMap(list -> check.merge(list, this.maxProblemsPerCheck)
                        .stream()
                        .map(Problem.class::cast))
                    .collect(Collectors.toCollection(ArrayList::new));
            }

            result.addAll(problemsForCheck);
        }

        return result;
    }

    public String translateMessage(Translatable message) {
        String output = message.format(this.fluentBundle);
        if (output.startsWith("Unknown messageID '")) {
            return output.substring("Unknown messageID '".length(), output.length() - 1);
        } else {
            return output;
        }
    }

    private static final Collection<Class<?>> CHECKS = new LinkedHashSet<>(
        new Reflections("de.firemage.autograder.core.check", Scanners.TypesAnnotated)
            .getTypesAnnotatedWith(ExecutableCheck.class)
    );

    public List<Check> findChecksForProblemTypes(Collection<ProblemType> problems) {
        return CHECKS
            .stream()
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

    private boolean isRequiredCheck(ExecutableCheck check, Collection<ProblemType> problems) {
        return check.enabled() && problems.stream().anyMatch(p -> List.of(check.reportedProblems()).contains(p));
    }
}
