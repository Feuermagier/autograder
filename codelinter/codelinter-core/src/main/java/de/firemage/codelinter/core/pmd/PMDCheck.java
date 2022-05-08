package de.firemage.codelinter.core.pmd;

import de.firemage.codelinter.core.Check;
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

    protected PMDCheck(String description, Rule rule) {
        this(description, List.of(rule));
    }

    protected PMDCheck(String description, List<Rule> rules) {
        this.description = description;
        this.rules = rules;

        for (Rule rule : rules) {
            if (rule.getMessage() == null) {
                rule.setMessage("");
            }
        }
    }

    protected static XPathRule createXPathRule(String name, String expression) {
        XPathRule rule = new XPathRule(XPathVersion.XPATH_2_0, expression);
        rule.setName(name);
        rule.setLanguage(LanguageRegistry.findLanguageByTerseName("java"));
        return rule;
    }
}
