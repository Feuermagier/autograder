package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.apache.commons.lang3.StringUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@ExecutableCheck(reportedProblems = {ProblemType.COMPLEX_REGEX})
public class RegexCheck extends IntegratedCheck {
    private static final int REGEX_SCORE_LIMIT = 2;
    private static final Map<String, Integer> REGEX_HINTS = new HashMap<>();

    static {
        REGEX_HINTS.put(")?", 1);
        REGEX_HINTS.put(")+", 1);
        REGEX_HINTS.put("*", 1);
        REGEX_HINTS.put("\\d", 1);
        REGEX_HINTS.put("\\w", 1);
        REGEX_HINTS.put("]?", 2);
        REGEX_HINTS.put("]+", 2);
        REGEX_HINTS.put("]*", 2);
        REGEX_HINTS.put("[^", 2);
        REGEX_HINTS.put("?=", 3); // Lookahead
        REGEX_HINTS.put("?<=", 3); // Lookbehind
        REGEX_HINTS.put("?!", 3); // Negative lookahead
        REGEX_HINTS.put("?<!", 3); // Negative lookbehind
        REGEX_HINTS.put("(?<", 3); // Named group
    }

    public RegexCheck() {
        super(new LocalizedMessage("complex-regex"));
    }

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
                    // Ignore short strings for performance reasons
                    return;
                }

                int score = REGEX_HINTS.entrySet().stream().mapToInt(e -> StringUtils.countMatches(value, e.getKey()) * e.getValue()).sum();
                if (score > REGEX_SCORE_LIMIT && isValidRegex(value)) {
                    addLocalProblem(literal, new LocalizedMessage("complex-regex"), ProblemType.COMPLEX_REGEX);
                }
            }
        });
    }

    private static boolean isValidRegex(String string) {
        try {
            Pattern.compile(string);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
