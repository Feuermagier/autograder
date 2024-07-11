package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.DO_NOT_HAVE_CONSTANTS_CLASS,
                                      ProblemType.EMPTY_INTERFACE })
public class InterfaceBadPractices extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInterface<?>>() {
            @Override
            public void process(CtInterface<?> ctInterface) {
                List<CtField<?>> fields = ctInterface.getFields();
                Set<CtMethod<?>> methods = ctInterface.getMethods();

                // check if the interface is empty
                if (methods.isEmpty() && fields.isEmpty()) {
                    addLocalProblem(
                        ctInterface,
                        new LocalizedMessage("empty-interface-exp"),
                        ProblemType.EMPTY_INTERFACE
                    );

                    return;
                }

                // check for interfaces with only fields, which are constants "classes"
                if (methods.isEmpty()) {
                    // interface is only used for constants
                    addLocalProblem(
                        ctInterface,
                        new LocalizedMessage("constants-class-exp"),
                        ProblemType.DO_NOT_HAVE_CONSTANTS_CLASS
                    );
                }
            }
        });
    }
}
