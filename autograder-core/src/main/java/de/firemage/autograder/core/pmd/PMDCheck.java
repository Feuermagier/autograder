package de.firemage.autograder.core.pmd;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.Check;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

import java.util.List;

public abstract class PMDCheck implements Check {

    private final String description;

    private final List<Rule> rules;

    private final String explanation;

    private final ProblemType problemType;

    protected PMDCheck(String description, String explanation, Rule rule,
                       ProblemType problemType) {
        this(description, explanation, List.of(rule), problemType);
    }

    protected PMDCheck(String description, Rule rule, ProblemType problemType) {
        this(description, null, List.of(rule), problemType);
    }

    protected PMDCheck(String description, List<Rule> rules, ProblemType problemType) {
        this(description, null, rules, problemType);
    }

    protected PMDCheck(String description, String explanation, List<Rule> rules,
                       ProblemType problemType) {
        this.description = description;
        this.explanation = explanation;
        this.rules = rules;
        this.problemType = problemType;

        for (Rule rule : rules) {
            if (rule.getMessage() == null) {
                rule.setMessage("");
            }
        }
    }

    protected static XPathRule createXPathRule(String name, String explanation, String expression) {
        XPathRule rule = new XPathRule(XPathVersion.XPATH_2_0, expression);
        rule.setName(name);
        rule.setMessage(explanation);
        rule.setLanguage(LanguageRegistry.findLanguageByTerseName("java"));
        return rule;
    }

    @Override
    public String getLinter() {
        return "PMD";
    }

    @Override
    public String getDescription() {
        return description;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public String getExplanation() {
        return explanation;
    }

    public ProblemType getProblemType() {
        return problemType;
    }
}
