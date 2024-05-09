package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.javadoc.api.elements.JavadocReference;
import spoon.javadoc.api.elements.JavadocVisitor;
import spoon.javadoc.api.parsing.JavadocParser;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtImportKind;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@ExecutableCheck(reportedProblems = { ProblemType.UNUSED_IMPORT })
public class UnusedImport extends IntegratedCheck {
    private static final Set<CtImportKind> SUPPORTED_IMPORTS = Set.of(
        CtImportKind.FIELD,
        CtImportKind.METHOD,
        CtImportKind.TYPE
        // wildcard imports have some annoying edge cases and should not occur in most submissions,
        // therefore, they are not supported
    );

    private static boolean isJavaLangImport(CtImport ctImport) {
        // check if the import is from the java.lang package, which is redundant

        CtPackageReference ctPackageReference = null;
        if (ctImport.getReference() instanceof CtTypeReference<?> ctTypeReference) {
            ctPackageReference = ctTypeReference.getTypeDeclaration().getPackage().getReference();
        } else if (ctImport.getReference() instanceof CtPackageReference packageReference) {
            ctPackageReference = packageReference;
        }

        return ctPackageReference != null && ctPackageReference.getQualifiedName().equals("java.lang");
    }

    private static boolean isInnerType(CtElement ctElement) {
        return ctElement instanceof CtType<?> ctType && SpoonUtil.isInnerClass(ctType);
    }

    private void reportProblem(CtImport ctImport) {
        this.addLocalProblem(
            ctImport,
            new LocalizedMessage(
                "unused-import",
                Map.of("import", ctImport)
            ),
            ProblemType.UNUSED_IMPORT
        );
    }

    private static class ReferenceFinder implements JavadocVisitor<List<CtReference>> {
        private final List<CtReference> references = new ArrayList<>();

        @Override
        public List<CtReference> defaultValue() {
            return this.references;
        }

        @Override
        public List<CtReference> visitReference(JavadocReference reference) {
            this.references.add(reference.getReference());

            return JavadocVisitor.super.visitReference(reference);
        }
    }

    private static boolean isReferencingTheSameElement(CtReference left, CtReference right) {
        return left.equals(right) || Objects.equals(SpoonUtil.getReferenceDeclaration(left), SpoonUtil.getReferenceDeclaration(right));
    }

    @SuppressWarnings("unchecked")
    private static boolean hasAnyJavadocUses(CtReference ctReference, Filter<? extends CtJavaDoc> filter) {
        return ctReference.getFactory()
            .getModel()
            .filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                filter,
                new TypeFilter<>(CtJavaDoc.class),
                ctJavaDoc -> {
                    JavadocParser parser = new JavadocParser(ctJavaDoc.getRawContent(), ctJavaDoc.getParent());

                    return parser.parse()
                        .stream()
                        .flatMap(element -> element.accept(new ReferenceFinder()).stream())
                        .anyMatch(otherReference -> isReferencingTheSameElement(ctReference, otherReference));
                }
            )).first(CtJavaDoc.class) != null;
    }

    private boolean isInSamePackage(CtElement ctElement, CtCompilationUnit ctCompilationUnit) {
        SourcePosition position = SpoonUtil.findPosition(ctElement);
        if (position == null) {
            return false;
        }

        CtPackage declaredPackage = position.getCompilationUnit().getDeclaredPackage();
        return Objects.equals(declaredPackage, ctCompilationUnit.getDeclaredPackage());
    }

    private void checkImport(CtImport ctImport, CtCompilationUnit ctCompilationUnit, Collection<? super CtElement> importedElements, CodeModel model) {
        // check if the import is from the java.lang package, which is redundant

        // inner class imports might not be redundant, therefore, they are skipped here
        if (isJavaLangImport(ctImport) && !(ctImport.getReference() instanceof CtTypeReference<?> ctTypeReference && SpoonUtil.isInnerClass(ctTypeReference.getTypeDeclaration()))) {
            this.reportProblem(ctImport);
            return;
        }

        CtNamedElement element = (CtNamedElement) SpoonUtil.getReferenceDeclaration(ctImport.getReference());

        // types from the same package are imported implicitly
        //
        // one can still explicitly import static methods like
        //
        // import static foo.A.doSomething;
        //
        // which is not redundant, because those are not imported implicitly.
        //
        // importing inner types is also not redundant, when they are used:
        // import foo.A.B;
        if (this.isInSamePackage(element, ctCompilationUnit) && !(element instanceof CtExecutable<?>) && !isInnerType(element)) {
            this.reportProblem(ctImport);
            return;
        }

        boolean isNewImport = importedElements.add(element);
        // duplicate imports are redundant
        if (!isNewImport) {
            this.reportProblem(ctImport);
            return;
        }


        Predicate<CtElement> isAllowed = ctElement -> true;

        if ((isJavaLangImport(ctImport) || this.isInSamePackage(element, ctCompilationUnit)) && isInnerType(element)) {
            // when the inner class is used, it's declaring type will be marked implicit
            // therefore, we need to remove all uses that do not use the inner class
            // e.g. Thread.UncaughtExceptionHandler (which is already imported)
            isAllowed = isAllowed.and(ctElement -> ctElement instanceof CtTypeReference<?> ctTypeReference && ctTypeReference.getDeclaringType() != null && ctTypeReference.getDeclaringType().isImplicit());
        }

        Predicate<CtElement> isSameFile = ctElement -> {
            SourcePosition position = SpoonUtil.findPosition(ctElement);

            return position != null && position.getCompilationUnit().equals(ctCompilationUnit);
        };

        isAllowed = isAllowed.and(isSameFile);


        // If there are no uses in the code, it might still be used in the javadoc.
        //
        // I don't think it is required to support imports that are only used in javadoc,
        // but spoon makes it easy to support it.
        boolean hasAnyUses = UsesFinder.getAllUses(element).filter(isAllowed).hasAny();
        if (!hasAnyUses) {
            hasAnyUses = hasAnyJavadocUses(ctImport.getReference(), isSameFile::test);
        }
        if (!hasAnyUses && ctImport.getImportKind() == CtImportKind.METHOD && element instanceof CtMethod<?> method) {
            // There is an edge case for static imports of overloaded methods in spoon,
            // where spoon arbitrarily chooses one of the overloaded methods to import.
            // This may not match the actually used method.
            // For example: we do `import static java.lang.String.valueOf;` and call `valueOf(5)`,
            // where 5 is implicitly an integer. Spoon however imports `valueOf(long)`, and we
            // detect an unused import.
            // Therefore, whether any overload of the method is used.
            var declaringType = method.getDeclaringType();
            if (declaringType != null) {
                for (var overloadedMethod : declaringType.getMethodsByName(method.getSimpleName())) {
                    if (UsesFinder.executableUses(overloadedMethod).filter(isSameFile).hasAny()) {
                        hasAnyUses = true;
                        break;
                    }
                }
            }
        }

        if (!hasAnyUses) {
            this.reportProblem(ctImport);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        SpoonUtil.visitCtCompilationUnit(staticAnalysis.getModel(), ctCompilationUnit -> {
            Collection<CtElement> importedElements = new HashSet<>();

            for (CtImport ctImport : ctCompilationUnit.getImports()) {
                if (!SUPPORTED_IMPORTS.contains(ctImport.getImportKind())) {
                    continue;
                }

                this.checkImport(ctImport, ctCompilationUnit, importedElements, staticAnalysis.getCodeModel());
            }
        });
    }
}
