package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.PRIMITIVE_WRAPPER_INSTANTIATION})
public class WrapperInstantiationCheck extends IntegratedCheck {
    private static <T> boolean isPrimitiveWrapper(CtTypeReference<T> ctTypeReference) {
        return SpoonUtil.isTypeEqualTo(
            ctTypeReference,
            Double.class, Float.class,
            Long.class, Integer.class,
            Short.class, Byte.class,
            Character.class, Boolean.class
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall<?> ctConstructorCall) {
                if (!ctConstructorCall.getPosition().isValidPosition() || ctConstructorCall.isImplicit()) {
                    return;
                }

                CtTypeReference<?> boxedType = ctConstructorCall.getType();

                if (isPrimitiveWrapper(boxedType) && ctConstructorCall.getArguments().size() == 1) {
                    // the primitive wrapper constructors should all have exactly one argument
                    CtExpression<?> value = ctConstructorCall.getArguments().get(0);
                    String suggestion = "%s".formatted(value);

                    // check if the argument is not the unboxed type
                    // for example Integer(String)
                    if (!boxedType.unbox().equals(SpoonUtil.getExpressionType(value))) {
                        suggestion = "%s.valueOf(%s)".formatted(boxedType, suggestion);
                    }

                    addLocalProblem(
                        ctConstructorCall,
                        new LocalizedMessage(
                            "suggest-replacement",
                            Map.of(
                                "suggestion", suggestion,
                                "original", ctConstructorCall
                            )
                        ),
                        ProblemType.PRIMITIVE_WRAPPER_INSTANTIATION
                    );
                }
            }
        });
    }
}
