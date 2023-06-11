package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.SpoonException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.SourcePositionHolder;
import spoon.reflect.cu.position.DeclarationSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExecutableCheck(reportedProblems = { ProblemType.IMPORT_TYPES })
public class ImportTypes extends IntegratedCheck {
    private static final Function<String, Pattern> ARRAY_TYPE_REGEX =
        (String ty) -> Pattern.compile("%s\\s*(?:\\[\\s*\\]|\\.\\.\\.)".formatted(Pattern.quote(ty)));
    private static final int MAX_SOURCE_LINES = 10;

    private static boolean isFullyQualifiedType(CtTypeReference<?> ctTypeReference) {
        // if this code breaks in the future, one might do this instead:
        // ctTypeReference.getOriginalSourceFragment().getSourceCode().startsWith(ctTypeReference.getQualifiedName())
        return !ctTypeReference.isSimplyQualified()
            && !ctTypeReference.isPrimitive()
            && !ctTypeReference.isGenerics()
            // to ignore String[]::new
            && ctTypeReference.getParent(CtExecutableReferenceExpression.class) == null
            && !SpoonUtil.isInnerClass(ctTypeReference);
    }

    // NOTE: this is cursed code, working around problems in spoon that have been ignored
    //       It might break at any time, maybe do ignore all arrays if that happens?
    private static String getCodeSnippet(SourcePositionHolder ctElement) {
        try {
            return ctElement.getOriginalSourceFragment().getSourceCode();
        } catch (SpoonException e) {
            // Invalid start/end interval. It overlaps multiple fragments.
            SourcePosition position = ctElement.getPosition();
            int start = position.getSourceStart();
            int end = position.getSourceEnd();
            if (position instanceof DeclarationSourcePosition declarationSourcePosition) {
                start = declarationSourcePosition.getDeclarationStart();
                end = declarationSourcePosition.getDeclarationEnd();
            }

            return position.getCompilationUnit().getOriginalSourceCode().substring(start, end + 1);
        }
    }

    private static SourcePosition resolveArraySourcePosition(CtArrayTypeReference<?> ctArrayTypeReference) {
        CtElement ctElement = ctArrayTypeReference;

        while (ctElement.isParentInitialized() && !ctElement.getPosition().isValidPosition()) {
            try {
                ctElement = ctElement.getParent();
            } catch (ParentNotInitializedException e) {
                return null;
            }
        }

        if (!ctElement.isParentInitialized() || !ctElement.getPosition().isValidPosition()) {
            return null;
        }

        SourcePosition position = ctElement.getPosition();

        // the number of lines the closest span covers:
        int lines = position.getEndLine() - position.getLine();

        if (lines > MAX_SOURCE_LINES) {
            return null;
        }

        String code = getCodeSnippet(ctElement);

        // NOTE: this only matches the "normal" array syntax: String[] value and not String value[]
        // the latter example is especially bad when it comes to things like methods, where you can do:
        // String myMethod()[]
        Matcher matcher = ARRAY_TYPE_REGEX.apply(ctArrayTypeReference.getArrayType().getSimpleName())
            .matcher(code);

        if (matcher.find()) {
            int start = matcher.start();
            int length = matcher.end() - start;

            return ctElement.getFactory().createSourcePosition(
                position.getCompilationUnit(),
                position.getSourceStart() + start,
                position.getSourceStart() + start + length,
                position.getCompilationUnit().getLineSeparatorPositions()
            );
        }

        return null;
    }

    private void reportProblem(CtTypeReference<?> ctTypeReference) {
        reportProblem(ctTypeReference.getPosition(), ctTypeReference);
    }

    private void reportProblem(SourcePosition sourcePosition, CtTypeReference<?> ctTypeReference) {
        addLocalProblem(
            CodePosition.fromSourcePosition(sourcePosition, ctTypeReference, this.getRoot()),
            new LocalizedMessage(
                "import-types",
                Map.of(
                    "type", ctTypeReference.getQualifiedName()
                )
            ),
            ProblemType.IMPORT_TYPES
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypeReference<?>>() {
            @Override
            public void process(CtTypeReference<?> ctTypeReference) {
                // Special case arrays, because they do not have a valid source position.
                //
                // The source position is not valid, because one can write String a[] and String[] a and spoon
                // does not support multi-spans
                if (ctTypeReference instanceof CtArrayTypeReference<?> ctArrayTypeReference
                    // skip nested arrays (they are already covered, because the code is executed on the parent as well)
                    && ctTypeReference.getParent(CtArrayTypeReference.class) == null) {
                    CtTypeReference<?> arrayType = ctArrayTypeReference.getArrayType();

                    SourcePosition position = resolveArraySourcePosition(ctArrayTypeReference);
                    if (isFullyQualifiedType(arrayType) && position != null) {
                        reportProblem(position, arrayType);
                    }

                    return;
                }

                if (ctTypeReference.isImplicit() || !ctTypeReference.getPosition().isValidPosition()) {
                    return;
                }

                if (isFullyQualifiedType(ctTypeReference)) {
                    reportProblem(ctTypeReference);
                }
            }
        });
    }
}
