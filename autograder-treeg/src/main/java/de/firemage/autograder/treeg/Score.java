package de.firemage.autograder.treeg;

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

public class Score {

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
            return 0.5;
        } else {
            return 0.1;
        }
    }

    private static double scoreAlternative(Alternative alternative) {
        return Math.exp(alternative.alternatives().size() / 5.0) * alternative.alternatives().stream().mapToDouble(Score::scoreNode).sum();
    }

    private static double scoreBoundaryMatcher(BoundaryMatcher matcher) {
        return 1.0;
    }

    private static double scoreCaptureGroupReference(CaptureGroupReference ref) {
        return 5.0;
    }

    private static double scoreChain(Chain chain) {
        return chain.children().stream().mapToDouble(Score::scoreNode).sum();
    }

    private static double scoreCharacterClass(CharacterClass c) {
        return (c.negated() ? 2.0 : 1.0) * c.ranges().stream().mapToDouble(Score::scoreCharacterClassEntry).sum();
    }

    private static double scoreCharacterClassEntry(CharacterClassEntry entry) {
        if (entry instanceof RegExCharacter c) {
            return scoreCharacter(c);
        } else if (entry instanceof CharacterRange r) {
            return scoreCharacterRange(r);
        } else {
            throw new AssertionError("Unreachable");
        }
    }

    private static double scoreCharacterRange(CharacterRange range) {
        return 5.0;
    }

    private static double scoreGroup(Group group) {
        double multiplier = switch (group.type()) {
            case CAPTURING -> 1.0;
            case NON_CAPTURING -> 2.0;
            case INDEPENDENT_NON_CAPTURING -> 5.0;
        };

        if (group.name() != null) {
            multiplier += 2.0;
        }

        if (group.flags() != null) {
            multiplier += Math.exp(group.flags().length());
        }

        return multiplier * scoreNode(group.root());
    }

    private static double scoreLookaround(Lookaround lookaround) {
        return 10.0 * scoreNode(lookaround.child());
    }

    private static double scorePredefinedCharacterClass(PredefinedCharacterClass c) {
        return switch (c.type()) {
            case ANY, DIGIT, WORD -> 0.5;
            case NON_DIGIT, WHITESPACE, NON_WORD -> 2.0;
            case HORIZONTAL_WHITESPACE, NON_HORIZONTAL_WHITESPACE, NON_WHITESPACE, VERTICAL_WHITESPACE, NON_VERTICAL_WHITESPACE -> 5.0;
        };
    }

    private static double scoreQuantifier(Quantifier quantifier) {
        return switch (quantifier.type()) {
            case AT_MOST_ONCE, ANY, AT_LEAST_ONCE -> 1.5;
            case TIMES, OPEN_RANGE, RANGE -> 2.0;
        } * scoreNode(quantifier.child());
    }
}
