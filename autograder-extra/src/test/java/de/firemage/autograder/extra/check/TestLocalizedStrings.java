package de.firemage.autograder.extra.check;

import de.firemage.autograder.core.LinterImpl;
import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.extra.check.naming.LinguisticNamingCheck;
import de.firemage.autograder.extra.check.naming.VariablesHaveDescriptiveNamesCheck;
import fluent.bundle.FluentBundle;
import fluent.bundle.FluentResource;
import fluent.syntax.AST.Message;
import fluent.syntax.parser.FTLParser;
import fluent.syntax.parser.FTLStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
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

import static org.junit.jupiter.api.Assertions.fail;

class TestLocalizedStrings {
    private static final Map<Class<?>, List<String>> MANUAL_MAPPING = Map.of(
        de.firemage.autograder.core.check.naming.ConstantsHaveDescriptiveNamesCheck.class, List.of(
            "constants-name-exp",
            "constants-name-exp-value"
        ),
        VariablesHaveDescriptiveNamesCheck.class, List.of(
            "variable-name-single-letter",
            "variable-is-abbreviation",
            "variable-name-type-in-name",
            "similar-identifier",
            "variable-redundant-number-suffix"
        ),
        de.firemage.autograder.core.check.api.IsEmptyReimplementationCheck.class, List.of(
            "suggest-replacement"
        ),
        de.firemage.autograder.core.check.api.OldCollectionCheck.class, List.of(
            "suggest-replacement"
        ),
        LinguisticNamingCheck.class, List.of(
            "linguistic-naming-getter",
            "linguistic-naming-setter",
            "linguistic-naming-boolean"
        )
    );

    private static final List<String> ALWAYS_USED_KEYS = List.of(
        "status-compiling",
        "status-compiling",
        "status-spotbugs",
        "status-pmd",
        "status-model",
        "status-integrated",
        "linter-pmd",
        "linter-integrated",
        "duplicate-code",
        "status-error-prone",
        "linter-error-prone",
        "merged-problems"
    );

    private static List<String> localizedKeys;
    private static FluentBundle englishBundle;
    private static FluentBundle germanBundle;

    private static String resolveKey(CtModel ctModel, List<? extends CtExpression<?>> args) {
        if (args.isEmpty() || !(SpoonUtil.resolveCtExpression(args.get(0)) instanceof CtLiteral<?> ctLiteral
            && ctLiteral.getValue() instanceof String key)) {
            throw new IllegalArgumentException("The first argument must be a string literal: " + args);
        }

        return key;
    }

    private static List<String> getAllLocalizedKeys(CtModel ctModel) {
        List<String> result = new ArrayList<>();
        for (CtType<?> ctType : ctModel.getAllTypes()) {
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

    private static Path resolvePath(String first, String... more) {
        return Path.of(first, more).toAbsolutePath().normalize();
    }

    @BeforeAll
    static void readChecksAndResources() {

        SpoonAPI launcher = new Launcher();
        // the `System.getProperty("user.dir")` is the path to the autograder-extra directory
        launcher.addInputResource(resolvePath(System.getProperty("user.dir"), "/src/main/").toString());
        // HACK: so it finds the core checks as well:
        launcher.addInputResource(resolvePath(System.getProperty("user.dir").replace("extra", "core"), "/src/main/").toString());

        launcher.getEnvironment().setComplianceLevel(JavaVersion.latest().getVersionNumber());

        launcher.buildModel();
        CtModel ctModel = launcher.getModel();

        localizedKeys = getAllLocalizedKeys(ctModel);

        englishBundle = LinterImpl.defaultLinter(Locale.ENGLISH).getFluentBundle();
        germanBundle = LinterImpl.defaultLinter(Locale.GERMAN).getFluentBundle();
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
