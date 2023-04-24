package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
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
import spoon.reflect.code.CtLiteral;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMPLEX_REGEX})
public class RegexCheck extends IntegratedCheck {
    private static final double MAX_ALLOWED_SCORE = 10.0;

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLiteral<String>>() {
            @Override
            public void process(CtLiteral<String> literal) {
                if (!SpoonUtil.isString(literal.getType())) {
                    return;
                }

                String value = literal.getValue();

                if (value.length() <= 4) {
                    // Ignore short strings for performance reasons (how complex can those regex be?!)
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
            case CAPTURING -> 2.0;
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
