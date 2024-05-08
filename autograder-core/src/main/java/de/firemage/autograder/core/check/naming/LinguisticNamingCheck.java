package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.CONFUSING_IDENTIFIER })
public class LinguisticNamingCheck extends IntegratedCheck {
    private static final Set<String> IGNORE_VARIABLES_WITH = Set.of("regex", "pattern");
    private static final Set<String> COMMON_BOOLEAN_GETTER_PREFIXES = Set.of(
        "is", "are", "can", "could", "must", "has", "have", "does", "will", "should", "would",
        "takes", "looks", "uses", "finds"
    );

    private static boolean hasBooleanPrefix(CtNamedElement ctNamedElement) {
        String name = ctNamedElement.getSimpleName();

        return IdentifierNameUtils.split(name)
            .findFirst()
            .map(COMMON_BOOLEAN_GETTER_PREFIXES::contains)
            .orElse(false);
    }

    private void reportProblem(String key, CtNamedElement ctNamedElement) {
        this.reportProblem(key, ctNamedElement, Map.of());
    }

    private void reportProblem(String key, CtNamedElement ctNamedElement, Map<String, String> extraArguments) {
        Map<String, String> arguments = new HashMap<>(extraArguments);
        arguments.put("name", ctNamedElement.getSimpleName());

        addLocalProblem(
            // adjust position so that only the name is highlighted
            CodePosition.fromSourcePosition(SpoonUtil.getNamePosition(ctNamedElement), ctNamedElement, this.getRoot()),
            new LocalizedMessage(key, arguments),
            ProblemType.CONFUSING_IDENTIFIER
        );
    }

    private <T> void checkCtVariable(CtVariable<T> ctVariable) {
        if (IGNORE_VARIABLES_WITH.stream().anyMatch(s -> ctVariable.getSimpleName().toLowerCase().contains(s))) {
            return;
        }

        if (hasBooleanPrefix(ctVariable) && !SpoonUtil.isBoolean(ctVariable)) {
            this.reportProblem(
                "linguistic-naming-boolean",
                ctVariable,
                Map.of("type", ctVariable.getType().toString())
            );
        }
    }

    private <T> void checkCtMethod(CtMethod<T> ctMethod, CodeModel model) {
        // to avoid duplicate reports, only report the first method declaration
        if (MethodHierarchy.isOverridingMethod(ctMethod)) {
            return;
        }

        if (hasBooleanPrefix(ctMethod) && !SpoonUtil.isBoolean(ctMethod)) {
            this.reportProblem(
                "linguistic-naming-boolean",
                ctMethod,
                Map.of("type", ctMethod.getType().toString())
            );

            return;
        }

        List<String> words = IdentifierNameUtils.split(ctMethod.getSimpleName()).toList();

        String prefix = words.get(0);

        if (prefix.equals("get") && SpoonUtil.isTypeEqualTo(ctMethod.getType(), void.class)) {
            // it is expected that a getter returns something
            this.reportProblem(
                "linguistic-naming-getter",
                ctMethod
            );

            return;
        }

        if (ctMethod.getBody() == null) {
            return;
        }

        if (prefix.equals("set") && isInvalidSetterReturnType(ctMethod) && SpoonUtil.getEffectiveStatements(ctMethod.getBody()).size() <= 3) {
            // it is expected that a setter returns nothing (void)
            this.reportProblem(
                "linguistic-naming-setter",
                ctMethod
            );
        }
    }

    private static boolean isInvalidSetterReturnType(CtMethod<?> ctMethod) {
        CtTypeReference<?> methodType = ctMethod.getType();

        // the expected return type of a setter is void
        if (SpoonUtil.isTypeEqualTo(methodType, void.class)) {
            return false;
        }

        // return this is allowed (useful for chaining)
        if (!ctMethod.isStatic()  && ctMethod.getDeclaringType() != null && methodType.equals(ctMethod.getDeclaringType().getReference())) {
            return false;
        }

        // returning the old value is allowed
        if (ctMethod.getParameters().size() == 1 && methodType.equals(ctMethod.getParameters().get(0).getType())) {
            return false;
        }

        // returning a boolean is allowed as well (for indicating success)
        return !SpoonUtil.isBoolean(ctMethod);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtNamedElement>() {
            @Override
            public void process(CtNamedElement ctNamedElement) {
                if (ctNamedElement.isImplicit() || !ctNamedElement.getPosition().isValidPosition()) {
                    return;
                }

                if (ctNamedElement instanceof CtMethod<?> ctMethod) {
                    checkCtMethod(ctMethod, staticAnalysis.getCodeModel());
                }

                if (ctNamedElement instanceof CtField<?> ctVariable) {
                    checkCtVariable(ctVariable);
                }

                if (ctNamedElement instanceof CtLocalVariable<?> ctVariable) {
                    checkCtVariable(ctVariable);
                }
            }
        });
    }
}
