package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = {ProblemType.JAVADOC_UNDOCUMENTED_THROWS})
public class ThrowsJavadocCheck extends IntegratedCheck {
    public ThrowsJavadocCheck() {
        super(new LocalizedMessage("javadoc-undocumented-throws"));
    }


    private void checkCtExecutable(CtExecutable<?> ctExecutable) {
        Optional<CtJavaDoc> doc = SpoonUtil.getJavadoc(ctExecutable);
        if (doc.isEmpty()) {
            return;
        }

        CtJavaDoc ctJavaDoc = doc.get();

        Set<String> documentedExceptions = ctJavaDoc.getTags().stream()
                .filter(tag -> tag.getType() == CtJavaDocTag.TagType.THROWS && tag.getParam() != null)
                .map(CtJavaDocTag::getParam)
                .collect(Collectors.toSet());

        List<CtThrow> ctThrows = ctExecutable.filterChildren(CtThrow.class::isInstance)
                .map(CtThrow.class::cast).list();
        for (CtThrow ctThrow : ctThrows) {
            if (ctThrow.getThrownExpression() instanceof CtConstructorCall<?> ctConstructorCall) {
                String name = ctConstructorCall.getType().getSimpleName();

                if (!documentedExceptions.contains(name)) {
                    addLocalProblem(
                            ctThrow,
                            new LocalizedMessage("javadoc-undocumented-throws", Map.of("exp", name)),
                            ProblemType.JAVADOC_UNDOCUMENTED_THROWS
                    );
                }
            }
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                if (ctMethod.isPrivate() || !ctMethod.getPosition().isValidPosition()) return;

                checkCtExecutable(ctMethod);

                super.visitCtMethod(ctMethod);
            }

            @Override
            public <T> void visitCtConstructor(CtConstructor<T> ctConstructor) {
                if (ctConstructor.isPrivate() || !ctConstructor.getPosition().isValidPosition()) return;

                checkCtExecutable(ctConstructor);

                super.visitCtConstructor(ctConstructor);
            }
        });
    }
}
