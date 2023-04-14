package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;


@ExecutableCheck(reportedProblems = { ProblemType.DO_NOT_HAVE_CONSTANTS_CLASS })
public class DoNotMakeConstantsClasses extends IntegratedCheck {
    private static final int FIELD_THRESHOLD = 12;

    public DoNotMakeConstantsClasses() {
        super(new LocalizedMessage("constants-class-exp"));
    }

    private boolean isConstantsClassLike(StaticAnalysis staticAnalysis, CtType<?> ctType) {
        // ignore anonymous classes
        return !ctType.isAnonymous()
            // should have at least one field
            && !ctType.getFields().isEmpty()
            // all fields should be static and effectively final (no assignments)
            && ctType.getFields().stream().allMatch(
                ctField -> ctField.isStatic() && SpoonUtil.isEffectivelyFinal(staticAnalysis, ctField)
            )
            // the class should not be abstract
            && !ctType.isAbstract()
            // the class should not implement anything
            && ctType.getSuperInterfaces().isEmpty()
            // the class should not have any inner classes
            && ctType.getNestedTypes().isEmpty()
            // the class itself should not be an inner class
            && !SpoonUtil.isInnerClass(ctType);
    }

    private boolean isConstantsEnum(StaticAnalysis staticAnalysis, CtType<?> ctType) {
        return ctType.isEnum()
               && ctType.getMethods().size() <= 3
               && ctType instanceof CtEnum<?> ctEnum
               && !ctEnum.getEnumValues().isEmpty()
               && ctEnum.getEnumValues().stream().map(ctEnumValue -> {
                   CtExpression<?> ctExpression = ctEnumValue.getDefaultExpression();
                   if (ctExpression == null) return null;

                   if (ctExpression instanceof CtConstructorCall<?> ctConstructorCall) {
                       return ctConstructorCall;
                   } else {
                       return null;
                   }
                })
                .filter(Objects::nonNull)
                .allMatch(ctConstructorCall -> {
                    if (ctConstructorCall.getArguments().size() != 1) return false;

                    CtTypeReference<?> ctTypeReference = ctConstructorCall.getArguments().get(0).getType();
                    return ctTypeReference != null && ctTypeReference.getQualifiedName().equals("java.lang.String");
                })
                && ctEnum.getEnumValues().size() > FIELD_THRESHOLD;
    }

    private boolean isConstantsClass(StaticAnalysis staticAnalysis, CtType<?> ctType) {
        return
            // for now only classes are detected (enums are more complicated)
            ctType.isClass()
            // the class should not extend anything
            && ctType.getSuperclass() == null
            && this.isConstantsClassLike(staticAnalysis, ctType)
            // must have no method or the number of fields must be above a threshold
            && (ctType.getMethods().isEmpty() || ctType.getFields().size() > FIELD_THRESHOLD);
    }
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> ctType) {
                if (isConstantsClass(staticAnalysis, ctType) || isConstantsEnum(staticAnalysis, ctType)) {
                    addLocalProblem(
                        ctType,
                        new LocalizedMessage("constants-class-exp"),
                        ProblemType.DO_NOT_HAVE_CONSTANTS_CLASS
                    );
                }
            }
        });
    }
}
