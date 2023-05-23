package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = {ProblemType.DEFAULT_PACKAGE_USED})
public class DefaultPackageCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        CtPackage defaultPackage = staticAnalysis.getModel().getRootPackage();

        if (!defaultPackage.getQualifiedName().isEmpty()) {
            return;
        }

        List<CtType<?>> typesInDefaultPackage = new ArrayList<>(defaultPackage.getTypes());

        if (typesInDefaultPackage.isEmpty()) {
            return;
        }

        CtType<?> ctType = typesInDefaultPackage.get(0);

        String positions = typesInDefaultPackage.stream()
            .map(CtType::getPosition)
            .map(SpoonUtil::formatSourcePosition)
            .collect(Collectors.joining(", "));

        this.addLocalProblem(
            ctType,
            new LocalizedMessage("default-package", Map.of("positions", positions)),
            ProblemType.DEFAULT_PACKAGE_USED
        );
    }
}
