package de.firemage.autograder.extra.pmd;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.Check;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;

import java.util.List;
import java.util.function.Function;

public abstract class PMDCheck implements Check {
    private static final Language JAVA_LANGUAGE = LanguageRegistry.PMD.getLanguageById("java");
    private final List<Rule> rules;

    private final Function<RuleViolation, Translatable> explanation;

    private final ProblemType problemType;

    protected PMDCheck(Translatable explanation, Rule rule, ProblemType problemType) {
        this((RuleViolation violation) -> explanation, List.of(rule), problemType);
    }

    protected PMDCheck(Function<RuleViolation, Translatable> explanation, Rule rule, ProblemType problemType) {
        this(explanation, List.of(rule), problemType);
    }

    protected PMDCheck(Translatable explanation, List<Rule> rules, ProblemType problemType) {
        this((RuleViolation violation) -> explanation, rules, problemType);
    }

    protected PMDCheck(Function<RuleViolation, Translatable> explanation, List<Rule> rules, ProblemType problemType) {
        this.explanation = explanation;
        this.rules = rules;
        this.problemType = problemType;

        for (Rule rule : rules) {
            if (rule.getLanguage() == null) {
                rule.setLanguage(JAVA_LANGUAGE);
            }

            if (rule.getMessage() == null) {
                rule.setMessage("");
            }
        }
    }

    @Override
    public LocalizedMessage getLinter() {
        return new LocalizedMessage("linter-pmd");
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Function<RuleViolation, Translatable> getExplanation() {
        return explanation;
    }

    public ProblemType getProblemType() {
        return problemType;
    }
}
