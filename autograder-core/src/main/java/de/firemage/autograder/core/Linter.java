package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.file.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.parallel.AnalysisResult;
import de.firemage.autograder.core.parallel.AnalysisScheduler;
import fluent.bundle.FluentBundle;
import fluent.bundle.FluentResource;
import fluent.functions.icu.ICUFunctionFactory;
import fluent.syntax.parser.FTLParser;
import fluent.syntax.parser.FTLStream;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
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
    private final ClassLoader classLoader;
    private final int maxProblemsPerCheck;

    private Linter(
        Locale locale,
        TempLocation tempLocation,
        int threads,
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
        this.classLoader = classLoader;
        this.maxProblemsPerCheck = maxProblemsPerCheck;
    }

    public static class Builder {
        private final Locale locale;
        private TempLocation tempLocation;
        private int threads;
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
        UploadedFile file,
        CheckConfiguration checkConfiguration,
        Consumer<LinterStatus> statusConsumer
    ) throws LinterException, IOException {
        var checks = this.findChecksForProblemTypes(checkConfiguration.problemsToReport());
        return this.checkFile(file, checkConfiguration, checks, statusConsumer);
    }

    private static <T> List<T> castUnsafe(Iterable<?> list, Class<? extends T> clazz) {
        List<T> result = new ArrayList<>();

        for (Object object : list) {
            result.add(clazz.cast(object));
        }

        return result;
    }

    public List<Problem> checkFile(
        UploadedFile file,
        CheckConfiguration checkConfiguration,
        Iterable<? extends Check> checks,
        Consumer<LinterStatus> statusConsumer
    ) throws LinterException, IOException {
        // the file is null if the student did not upload source code
        if (file == null) {
            return new ArrayList<>();
        }

        Map<CodeLinter<?>, List<Check>> linterChecks = new IdentityHashMap<>();

        List<? extends CodeLinter<?>> codeLinters = this.findCodeLinter();
        for (Check check : checks) {
            for (CodeLinter<?> linter : codeLinters) {
                if (linter.supportedCheckType().isInstance(check)) {
                    linterChecks.computeIfAbsent(linter, key -> new ArrayList<>()).add(check);
                    // only add each check to one linter
                    break;
                }
            }
        }

        AnalysisScheduler scheduler = new AnalysisScheduler(this.threads, classLoader);

        AnalysisResult result;
        try (TempLocation tempLinterLocation = this.tempLocation.createTempDirectory("linter")) {
            for (var entry : linterChecks.entrySet()) {
                CodeLinter linter = entry.getKey();
                var targetCheckType = linter.supportedCheckType();
                var associatedChecks = castUnsafe(entry.getValue(), targetCheckType);

                // skip linting if there are no checks for this linter
                // some linters might do stuff even if there are no checks
                if (associatedChecks.isEmpty()) {
                    continue;
                }

                scheduler.submitTask((s, reporter) -> {
                    reporter.reportProblems(linter.lint(
                        file,
                        tempLinterLocation,
                        this.classLoader,
                        associatedChecks,
                        statusConsumer
                    ));
                });
            }

            result = scheduler.collectProblems();
            if (result.failed()) {
                throw new LinterException(result.thrownException());
            }
        }

        List<Problem> unreducedProblems = result.problems();
        if (!checkConfiguration.problemsToReport().isEmpty()) {
            unreducedProblems = result
                .problems()
                .stream()
                .filter(problem -> checkConfiguration.problemsToReport().contains(problem.getProblemType()))
                .toList();
        }

        // filter out problems in excluded classes
        var excludedClasses = checkConfiguration.excludedClasses();
        if (excludedClasses != null && !excludedClasses.isEmpty()) {
            unreducedProblems = unreducedProblems.stream()
                    .filter(problem -> !checkConfiguration.excludedClasses()
                            .contains(problem.getPosition().file().getName().replace(".java", "")))
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

            int targetNumberOfProblems = Math.min(
                this.maxProblemsPerCheck,
                entry.getKey().maximumProblems().orElse(this.maxProblemsPerCheck)
            );

            // then go through each check and merge the problems if they exceed the maxProblemsPerCheck
            if (problemsForCheck.size() > targetNumberOfProblems) {
                // further partition the problems by their ProblemType
                // (one does not want to merge different types of problems):
                Map<ProblemType, List<Problem>> problemsByType = problemsForCheck.stream()
                    .collect(Collectors.groupingBy(Problem::getProblemType, LinkedHashMap::new, Collectors.toList()));

                problemsForCheck = problemsByType.values()
                    .stream()
                    .flatMap(list -> check.merge(list, targetNumberOfProblems).stream())
                    .collect(Collectors.toCollection(ArrayList::new));
            }

            result.addAll(problemsForCheck);
        }

        return result;
    }

    public String translateMessage(Translatable message) {
        String output = message.format(this.fluentBundle);

        if (output.startsWith("Unknown messageID '")) {
            throw new IllegalStateException(output);
        }

        return output;
    }

    private static final Collection<Class<?>> CHECKS = new LinkedHashSet<>(
        new Reflections("de.firemage.autograder.", Scanners.TypesAnnotated)
            .getTypesAnnotatedWith(ExecutableCheck.class)
    );

    public List<Check> findChecksForProblemTypes(Collection<ProblemType> problems) {
        return CHECKS
            .stream()
            .filter(check -> isRequiredCheck(check.getAnnotation(ExecutableCheck.class), problems))
            .map(check -> {
                try {
                    return (Check) check.getConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to instantiate check " + check.getName(), e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(check.getName() + " does not inherit from Check");
                }
            })
            .toList();
    }

    private static final Collection<Class<?>> CODE_LINTER = new LinkedHashSet<>(
        new Reflections("de.firemage.autograder.", Scanners.SubTypes)
            .getSubTypesOf(CodeLinter.class)
    );

    public List<? extends CodeLinter<?>> findCodeLinter() {
        return CODE_LINTER
            .stream()
            .map(linter -> {
                try {
                    return (CodeLinter<?>) linter.getConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to instantiate check " + linter.getName(), e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(linter.getName() + " does not inherit from Check");
                }
            })
            .toList();
    }


    private boolean isRequiredCheck(ExecutableCheck check, Collection<ProblemType> problems) {
        return check.enabled() && problems.stream().anyMatch(p -> List.of(check.reportedProblems()).contains(p));
    }
}
