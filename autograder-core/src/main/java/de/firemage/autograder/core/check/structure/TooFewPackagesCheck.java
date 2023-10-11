package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.TOO_FEW_PACKAGES})
public class TooFewPackagesCheck extends IntegratedCheck {
    public static final int MAX_CLASSES_PER_PACKAGE = 8;
    public static final String LOCALIZED_MESSAGE_KEY = "too-few-packages";

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        List<CtPackage> packages = staticAnalysis.getModel().getAllPackages()
                .stream()
                .filter(CtPackage::hasTypes)
                .toList();

        if (packages.size() == 1
                && packages.stream().anyMatch(ctPackage -> ctPackage.getTypes().size() > MAX_CLASSES_PER_PACKAGE)) {

            Optional<CtType<?>> ctType = packages.get(0).getTypes().stream().findFirst();
            ctType.ifPresent(type ->
                this.addLocalProblem(
                        type,
                        new LocalizedMessage(LOCALIZED_MESSAGE_KEY),
                        ProblemType.TOO_FEW_PACKAGES
                ));
        }
    }


}
