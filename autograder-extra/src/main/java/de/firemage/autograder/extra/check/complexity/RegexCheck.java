package de.firemage.autograder.extra.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import de.firemage.autograder.treeg.InvalidRegExSyntaxException;
import de.firemage.autograder.treeg.RegExParser;
import de.firemage.autograder.treeg.RegularExpression;
import de.firemage.autograder.treeg.ast.Alternative;
import de.firemage.autograder.treeg.ast.BoundaryMatcher;
import de.firemage.autograder.treeg.ast.CaptureGroupReference;
import de.firemage.autograder.treeg.ast.Chain;
import de.firemage.autograder.treeg.ast.CharacterClass;
import de.firemage.autograder.treeg.ast.CharacterClassEntry;
import de.firemage.autograder.treeg.ast.CharacterRange;
import de.firemage.autograder.treeg.ast.Group;
import de.firemage.autograder.treeg.ast.Lookaround;
import de.firemage.autograder.treeg.ast.PredefinedCharacterClass;
import de.firemage.autograder.treeg.ast.Quantifier;
import de.firemage.autograder.treeg.ast.RegExCharacter;
import de.firemage.autograder.treeg.ast.RegExNode;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMPLEX_REGEX})
public class RegexCheck extends IntegratedCheck {
    public static final double MAX_ALLOWED_SCORE = 24.0;
    private static final List<String> REGEX_HINTS = List.of("?", "<", ">", "+", "*", "[", "]", "$", "^", "|", "\\");
    private static final int MIN_REGEX_HINTS = 2;

    private static boolean hasComment(CtElement ctElement) {
        return (!ctElement.getComments().isEmpty()
            // test-framework comments start with //#, which should be ignored
            && ctElement.getComments().stream().anyMatch(ctComment -> !ctComment.getContent().startsWith("#")))
            || ctElement.getParent() instanceof CtVariable<?> ctVariable && hasComment(ctVariable);
    }

    private static boolean looksLikeRegex(String value) {
        return REGEX_HINTS.stream().filter(value::contains).count() >= MIN_REGEX_HINTS;
    }

    private static boolean isRegexInvocation(CtInvocation<?> ctInvocation) {
        CtExecutableReference<?> ctExecutable = ctInvocation.getExecutable();

        // for super invocations the target is null
        if (ctInvocation.getTarget() == null) {
            return false;
        }

        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), java.util.regex.Pattern.class)
            && List.of("matches", "compile").contains(ctExecutable.getSimpleName())
            || SpoonUtil.isTypeEqualTo(ctInvocation.getTarget().getType(), java.lang.String.class)
            && (
            SpoonUtil.isSignatureEqualTo(ctExecutable, boolean.class, "matches", String.class)
                || SpoonUtil.isSignatureEqualTo(ctExecutable, String.class, "replaceAll", String.class, String.class)
                || SpoonUtil.isSignatureEqualTo(ctExecutable, String.class, "replaceFirst", String.class, String.class)
                || SpoonUtil.isSignatureEqualTo(ctExecutable, String[].class, "split", String.class)
                || SpoonUtil.isSignatureEqualTo(ctExecutable, String[].class, "split", String.class, int.class)
        );
    }

    private static boolean isInAllowedContext(CtLiteral<?> ctLiteral) {
        CtElement parent = ctLiteral.getParent();
        if (parent instanceof CtVariable<?> ctVariable
            && SpoonUtil.isEffectivelyFinal(ctVariable)) {
            // Check if the variable is only used in a regex invocation (e.g. Pattern.compile)
            return UsesFinder.variableUses(ctVariable)
                .hasAnyAndAllMatch(ctVariableAccess -> ctVariableAccess.getParent() instanceof CtInvocation<?> ctInvocation
                    && isRegexInvocation(ctInvocation));
        }

        return parent instanceof CtInvocation<?> ctInvocation && isRegexInvocation(ctInvocation);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLiteral<String>>() {
            @Override
            public void process(CtLiteral<String> literal) {
                if (!SpoonUtil.isString(literal.getType()) || !isInAllowedContext(literal)) {
                    return;
                }

                String value = literal.getValue();

                if (value.length() <= 4) {
                    // Ignore short strings for performance reasons (how complex can those regex be?!)
                    return;
                }

                if (hasComment(literal)) {
                    // Ignore regex with comments explaining what they do
                    return;
                }

                try {
                    RegularExpression regex = RegExParser.parse(value);

                    if (regex.root() instanceof Chain chain && chain.children().stream().allMatch(c -> c instanceof RegExCharacter)) {
                        // Normal string
                        return;
                    }

                    double score = scoreRegEx(regex);
                    if (score > MAX_ALLOWED_SCORE) {
                        addLocalProblem(
                                literal,
                                new LocalizedMessage("complex-regex", Map.of("score", score, "max", MAX_ALLOWED_SCORE)),
                                ProblemType.COMPLEX_REGEX
                        );
                    }
                } catch (InvalidRegExSyntaxException e) {
                    // Not a valid regex
                }
            }
        });
    }

    public static double scoreRegEx(RegularExpression regex) {
        return scoreNode(regex.root());
    }

    private static double scoreNode(RegExNode node) {
        // This would be so much nicer with switch patterns...
        if (node instanceof RegExCharacter c) {
            return scoreCharacter(c);
        } else if (node instanceof Alternative a) {
            return scoreAlternative(a);
        } else if (node instanceof BoundaryMatcher b) {
            return scoreBoundaryMatcher(b);
        } else if (node instanceof CaptureGroupReference c) {
            return scoreCaptureGroupReference(c);
        } else if (node instanceof Chain c) {
            return scoreChain(c);
        } else if (node instanceof CharacterClass c) {
            return scoreCharacterClass(c);
        } else if (node instanceof Group g) {
            return scoreGroup(g);
        } else if (node instanceof Lookaround l) {
            return scoreLookaround(l);
        } else if (node instanceof PredefinedCharacterClass p) {
            return scorePredefinedCharacterClass(p);
        } else if (node instanceof Quantifier q) {
            return scoreQuantifier(q);
        } else {
            throw new AssertionError("Unreachable");
        }
    }

    private static double scoreCharacter(RegExCharacter character) {
        if (character.escaped()) {
            return 2.0;
        } else {
            return 0.0;
        }
    }

    private static double scoreAlternative(Alternative alternative) {
        return Math.exp(alternative.alternatives().size()) * alternative.alternatives().stream().mapToDouble(RegexCheck::scoreNode).sum();
    }

    private static double scoreBoundaryMatcher(BoundaryMatcher matcher) {
        return 1.0;
    }

    private static double scoreCaptureGroupReference(CaptureGroupReference ref) {
        return 10.0;
    }

    private static double scoreChain(Chain chain) {
        return chain.children().stream().mapToDouble(RegexCheck::scoreNode).sum() + 1.0;
    }

    private static double scoreCharacterClass(CharacterClass c) {
        return (c.negated() ? 4.0 : 1.0) * c.ranges().stream().mapToDouble(RegexCheck::scoreCharacterClassEntry).sum();
    }

    private static double scoreCharacterClassEntry(CharacterClassEntry entry) {
        if (entry instanceof RegExCharacter c) {
            return scoreCharacter(c) + 0.1;
        } else if (entry instanceof CharacterRange r) {
            return scoreCharacterRange(r);
        } else {
            throw new AssertionError("Unreachable");
        }
    }

    private static double scoreCharacterRange(CharacterRange range) {
        return 2.0;
    }

    private static double scoreGroup(Group group) {
        double multiplier = switch (group.type()) {
            case CAPTURING -> 1.5;
            case NON_CAPTURING -> 10.0;
            case INDEPENDENT_NON_CAPTURING -> 100.0;
        };

        if (group.name() != null) {
            multiplier += 10.0;
        }

        if (group.flags() != null) {
            multiplier += Math.exp(group.flags().length() + 2);
        }

        return multiplier * scoreNode(group.root());
    }

    private static double scoreLookaround(Lookaround lookaround) {
        return 10.0 * scoreNode(lookaround.child());
    }

    private static double scorePredefinedCharacterClass(PredefinedCharacterClass c) {
        return switch (c.type()) {
            case ANY, DIGIT, WORD -> 1.0;
            case NON_DIGIT, WHITESPACE, NON_WORD -> 5.0;
            case HORIZONTAL_WHITESPACE, NON_HORIZONTAL_WHITESPACE, NON_WHITESPACE, VERTICAL_WHITESPACE, NON_VERTICAL_WHITESPACE ->
                    10.0;
        };
    }

    private static double scoreQuantifier(Quantifier quantifier) {
        return switch (quantifier.type()) {
            case AT_MOST_ONCE, ANY, AT_LEAST_ONCE -> 2.0;
            case TIMES, OPEN_RANGE, RANGE -> 5.0;
        } * scoreNode(quantifier.child());
    }
}
