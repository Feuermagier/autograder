package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.check.Check;
import lombok.Getter;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;
import java.util.List;

public abstract class PMDCheck implements Check {

    @Getter
    private final String description;

    @Getter
    private final List<Rule> rules;

    @Getter
    private final String explanation;

    protected PMDCheck(String description, String explanation, Rule rule) {
        this(description, explanation, List.of(rule));
    }

    protected PMDCheck(String description, Rule rule) {
        this(description, null, List.of(rule));
    }

    protected PMDCheck(String description, List<Rule> rules) {
        this(description, null, rules);
    }

    protected PMDCheck(String description, String explanation, List<Rule> rules) {
        this.description = description;
        this.explanation = explanation;
        this.rules = rules;

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
}
