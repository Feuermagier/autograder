package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackageDeclaration;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = {ProblemType.PACKAGE_NAMING_CONVENTION})
public class PackageNamingConvention extends IntegratedCheck {
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*");

    private static <T> Set<T> intersection(Set<? extends T> left, Collection<T> right) {
        Set<T> result = new HashSet<>(left);
        result.retainAll(right);
        return result;
    }

    private void visitCtPackageDeclaration(StaticAnalysis staticAnalysis, Consumer<? super CtPackageDeclaration> lambda) {
        // it is not possible to visit CtPackageDeclaration through the processor API.
        //
        // in https://github.com/INRIA/spoon/issues/5168 the below code is mentioned as a workaround:
        staticAnalysis.getModel()
            .getAllTypes()
            .stream()
            .map(CtType::getPosition)
            .filter(SourcePosition::isValidPosition)
            .map(SourcePosition::getCompilationUnit)
            .map(CtCompilationUnit::getPackageDeclaration)
            .forEach(lambda);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        Set<CtPackageDeclaration> invalidDeclarations = new LinkedHashSet<>();
        Collection<String> markedPackages = new HashSet<>();

        this.visitCtPackageDeclaration(staticAnalysis, ctPackageDeclaration -> {
            if (ctPackageDeclaration.isImplicit()
                || !ctPackageDeclaration.getPosition().isValidPosition()
                || ctPackageDeclaration.getReference() == null) {
                return;
            }

            String packageName = ctPackageDeclaration.getReference().getQualifiedName();

            // collect all the invalid parts of the package name:
            Set<String> invalidParts = Arrays.stream(packageName.split("\\.", -1))
                .filter(name -> !PACKAGE_NAME_PATTERN.matcher(name).matches())
                .collect(Collectors.toSet());

            // check if some parts have already been marked as invalid
            if (!intersection(invalidParts, markedPackages).isEmpty()) {
                // if so, the problem has already been reported somewhere
                // this is mainly intended to reduce the amount of bloat (keep the annotations to a minimum)
                return;
            }

            markedPackages.addAll(invalidParts);

            invalidDeclarations.add(ctPackageDeclaration);
        });

        List<CtPackageDeclaration> declarations = new ArrayList<>(invalidDeclarations);

        if (declarations.isEmpty()) {
            return;
        }

        CtPackageDeclaration ctPackageDeclaration = declarations.get(0);

        String positions = declarations.stream()
            .map(CtPackageDeclaration::getPosition)
            .map(SpoonUtil::formatSourcePosition)
            .collect(Collectors.joining(", "));

        this.addLocalProblem(
            ctPackageDeclaration,
            new LocalizedMessage("package-naming-convention", Map.of("positions", positions)),
            ProblemType.PACKAGE_NAMING_CONVENTION
        );
    }
}
