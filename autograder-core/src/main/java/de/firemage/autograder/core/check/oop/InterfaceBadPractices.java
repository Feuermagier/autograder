package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.support.reflect.CtExtendedModifier;

import java.util.List;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.CONSTANT_IN_INTERFACE, ProblemType.DO_NOT_HAVE_CONSTANTS_CLASS,
                                      ProblemType.STATIC_INTERFACE, ProblemType.STATIC_METHOD_IN_INTERFACE,
                                      ProblemType.EMPTY_INTERFACE })
public class InterfaceBadPractices extends IntegratedCheck {
    public InterfaceBadPractices() {
        super(new LocalizedMessage("interface-bad-practices-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
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
                } else if (!fields.isEmpty()) {
                    // interfaces should not have fields:
                    for (CtField<?> field : fields) {
                        addLocalProblem(
                            field,
                            new LocalizedMessage("constants-interfaces-exp"),
                            ProblemType.CONSTANT_IN_INTERFACE
                        );
                    }
                }

                for (CtMethod<?> method : methods) {
                    // static methods in interfaces should be avoided
                    if (method.isStatic()) {
                        addLocalProblem(
                            method,
                            new LocalizedMessage("interface-static-method-exp"),
                            ProblemType.STATIC_METHOD_IN_INTERFACE
                        );
                    }
                }

                // static modifier can be added to interfaces inside classes,
                // but is redundant and should be avoided
                for (CtExtendedModifier ctExtendedModifier : ctInterface.getExtendedModifiers()) {
                    if (!ctExtendedModifier.isImplicit() && ctExtendedModifier.getKind() == ModifierKind.STATIC) {
                        addLocalProblem(
                            ctInterface,
                            new LocalizedMessage("interface-static-exp"),
                            ProblemType.STATIC_INTERFACE
                        );
                    }
                }
            }
        });
    }
}
