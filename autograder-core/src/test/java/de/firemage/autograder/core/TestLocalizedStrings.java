package de.firemage.autograder.core;

import de.firemage.autograder.core.integrated.SpoonUtil;
import fluent.bundle.FluentBundle;
import fluent.bundle.FluentResource;
import fluent.syntax.AST.Message;
import fluent.syntax.parser.FTLParser;
import fluent.syntax.parser.FTLStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

class TestLocalizedStrings {
    private static final Map<Class<?>, List<String>> MANUAL_MAPPING = Map.of(
        de.firemage.autograder.core.check.complexity.RepeatedMathOperationCheck.class, List.of(
            "repeated-math-operation-mul",
            "repeated-math-operation-plus"
        ),
        de.firemage.autograder.core.check.naming.ConstantsHaveDescriptiveNamesCheck.class, List.of(
            "constants-name-exp",
            "constants-name-exp-value"
        ),
        de.firemage.autograder.core.check.naming.VariablesHaveDescriptiveNamesCheck.class, List.of(
            "variable-name-single-letter",
            "variable-name-type",
            "variable-name-type-in-name",
            "similar-identifier",
            "variable-redundant-number-suffix"
        )
    );

    private static final List<String> ALWAYS_USED_KEYS = List.of(
        "status-compiling",
        "status-compiling",
        "status-spotbugs",
        "status-pmd",
        "status-cpd",
        "status-model",
        "status-docker",
        "status-tests",
        "status-integrated",
        "linter-cpd",
        "linter-spotbugs",
        "linter-pmd",
        "linter-integrated",
        "duplicate-code",
        "status-error-prone",
        "linter-error-prone"
    );

    private static List<String> localizedKeys;
    private static FluentBundle englishBundle;
    private static FluentBundle germanBundle;

    static CtPackage findBasePackage(CtModel ctModel) {
        final CtPackage[] basePackage = {null};
        ctModel.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> type) {
                if (type.getPackage() == null || type.getPackage().getQualifiedName().startsWith("java.")) {
                    return;
                }

                if (basePackage[0] == null) {
                    basePackage[0] = type.getPackage();
                    return;
                }

                var typePackage = type.getPackage().getQualifiedName();
                while (!typePackage.startsWith(basePackage[0].getQualifiedName())) {
                    basePackage[0] = basePackage[0].getDeclaringPackage();
                }
            }
        });

        return basePackage[0];
    }

    static Set<CtType<?>> getAllTypes(CtPackage ctPackage) {
        Set<CtType<?>> ctTypes = ctPackage.getTypes();

        for (CtPackage subPackage : ctPackage.getPackages()) {
            ctTypes.addAll(getAllTypes(subPackage));
        }

        return ctTypes;
    }

    private static String resolveKey(CtModel ctModel, List<? extends CtExpression<?>> args) {
        if (args.isEmpty() || !(SpoonUtil.resolveCtExpression(args.get(0)) instanceof CtLiteral<?> ctLiteral
            && ctLiteral.getValue() instanceof String key)) {
            throw new IllegalArgumentException("The first argument must be a string literal: " + args);
        }

        return key;
    }

    static List<String> getAllLocalizedKeys(CtModel ctModel, CtPackage basePackage) {
        CtPackage checkPackage = basePackage.getPackage("check");

        List<String> result = new ArrayList<>();
        for (CtType<?> ctType : getAllTypes(checkPackage)) {
            // skip non-checks
            if (ctType.getAnnotations()
                .stream()
                .noneMatch(a -> a.getAnnotationType()
                    .getQualifiedName()
                    .equals("de.firemage.autograder.core.check.ExecutableCheck")
                )
            ) {
                continue;
            }

            Optional<Map.Entry<Class<?>, List<String>>> manualMapping = MANUAL_MAPPING.entrySet().stream()
                .filter(entry -> SpoonUtil.isTypeEqualTo(ctType.getReference(), entry.getKey()))
                .findAny();

            if (manualMapping.isPresent()) {
                result.addAll(manualMapping.get().getValue());
                continue;
            }

            result.addAll(ctType
                .filterChildren(ctElement -> ctElement instanceof CtConstructorCall<?> ctConstructorCall
                    && ctConstructorCall.getType().getSimpleName().equals("LocalizedMessage"))
                .map(ctElement -> resolveKey(ctModel, ((CtConstructorCall<?>) ctElement).getArguments()))
                .list()
            );

            // add XPath rules
            result.addAll(ctType.filterChildren(ctElement -> ctElement instanceof CtInvocation<?> ctInvocation
                    && ctInvocation.getExecutable().getSimpleName().equals("createXPathRule")
                )
                .map(ctElement -> ((CtInvocation<?>) ctElement).getArguments().get(1))
                .map(key -> resolveKey(ctModel, List.of((CtExpression<?>) key)))
                .list());

            // PMD Rule#setMessage(String) calls
            result.addAll(ctType.filterChildren(ctElement -> ctElement instanceof CtInvocation<?> ctInvocation
                    && ctInvocation.getExecutable().getSimpleName().equals("setMessage")
                    && SpoonUtil.isTypeEqualTo(ctInvocation.getTarget().getType(), net.sourceforge.pmd.Rule.class)
                )
                .map(ctElement -> ((CtInvocation<?>) ctElement).getArguments().get(0))
                .map(key -> resolveKey(ctModel, List.of((CtExpression<?>) key)))
                .list());
        }

        result.addAll(ALWAYS_USED_KEYS);

        return result;
    }

    @BeforeAll
    static void readChecksAndResources() {
        // the `System.getProperty("user.dir")` is the path to the autograder-core directory
        Path path = Path.of(System.getProperty("user.dir"), "/src/main/").toAbsolutePath().normalize();

        SpoonAPI launcher = new Launcher();
        launcher.addInputResource(path.toString());
        launcher.getEnvironment().setComplianceLevel(17);

        launcher.buildModel();
        CtModel ctModel = launcher.getModel();

        CtPackage basePackage = findBasePackage(ctModel);

        localizedKeys = getAllLocalizedKeys(ctModel, basePackage);

        englishBundle = Linter.defaultLinter(Locale.ENGLISH).getFluentBundle();
        germanBundle = Linter.defaultLinter(Locale.GERMAN).getFluentBundle();
    }

    Collection<String> findMissingKeys(FluentBundle bundle) {
        return localizedKeys.stream().filter(key -> bundle.getMessage(key).isEmpty()).toList();
    }

    @Test
    void allEnglishKeysArePresent() {
        Collection<String> missingKeys = this.findMissingKeys(englishBundle);

        if (!missingKeys.isEmpty()) {
            fail("The following keys are missing from the `strings.en.ftl`: " + missingKeys);
        }
    }

    @Test
    void allGermanKeysArePresent() {
        Collection<String> missingKeys = this.findMissingKeys(germanBundle);

        if (!missingKeys.isEmpty()) {
            fail("The following keys are missing from the `strings.de.ftl`: " + missingKeys);
        }
    }

    FluentResource readFluentResource(Locale locale) {
        String filename = switch (locale.getLanguage()) {
            case "de" -> "/strings.de.ftl";
            case "en" -> "/strings.en.ftl";
            default -> throw new IllegalArgumentException("No translation available for the locale " + locale);
        };
        try {
            FluentResource resource = FTLParser.parse(FTLStream.of(
                new String(this.getClass().getResourceAsStream(filename).readAllBytes(), StandardCharsets.UTF_8)
            ));
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    Collection<String> findUnusedKeys(Locale locale) {
        FluentResource resource = this.readFluentResource(locale);

        return resource.entries()
            .stream()
            .filter(entry -> entry instanceof Message)
            .map(entry -> ((Message) entry).identifier().name())
            .filter(key -> localizedKeys.stream().noneMatch(call -> call.equals(key)))
            .toList();
    }

    @Test
    void allEnglishKeysAreUsed() {
        Collection<String> unusedKeys = this.findUnusedKeys(Locale.ENGLISH);

        if (!unusedKeys.isEmpty()) {
            fail("The following keys are not used in the checks: " + unusedKeys);
        }
    }

    @Test
    void allGermanKeysAreUsed() {
        Collection<String> unusedKeys = this.findUnusedKeys(Locale.GERMAN);

        if (!unusedKeys.isEmpty()) {
            fail("The following keys are not used in the checks: " + unusedKeys);
        }
    }
}
