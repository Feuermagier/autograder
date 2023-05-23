package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.UI_INPUT_SEPARATION, ProblemType.UI_OUTPUT_SEPARATION })
public class IOUISeparation extends IntegratedCheck {
    private final Map<String, SourcePosition> systemInvocations = new LinkedHashMap<>();
    private final Map<String, SourcePosition> scannerInvocations = new LinkedHashMap<>();

    private boolean hasAccessedSystem(CtInvocation<?> ctInvocation) {
        // System.out.println(String) is a CtInvocation of the method println(String)
        // The target of the invocation is System.out, which is a CtFieldRead
        // (reads the field `out` of the class `System`)
        return ctInvocation.getTarget() instanceof CtFieldRead<?> ctFieldRead
            // access the type that contains the field
            && ctFieldRead.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            // check if the accessed field is out or err
            && List.of("out", "err").contains(ctFieldRead.getVariable().getSimpleName())
            // to narrow it down: the field out and err are static and final
            && ctFieldRead.getVariable().isStatic()
            && ctFieldRead.getVariable().isFinal()
            // check that the Attribute is accessed from the class System
            && ctInvocation.getFactory().Type().createReference(System.class).equals(ctTypeAccess.getAccessedType());
    }

    /**
     * Checks if the given invocation accesses a Scanner.
     *
     * @param ctInvocation the invocation to check
     * @return true if the invocation called a method on a Scanner, false otherwise
     */
    private boolean hasAccessedScanner(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtVariableRead<?> ctVariableRead
            && ctVariableRead.getVariable() != null // just to be sure
            && ctInvocation.getFactory().Type().createReference(java.util.Scanner.class)
                           .equals(ctVariableRead.getVariable().getType());
    }

    private Optional<String> getParentName(CtInvocation<?> ctInvocation) {
        return Optional.ofNullable(ctInvocation.getParent(CtType.class))
                       .map(CtTypeInformation::getQualifiedName);
    }

    private void checkCtInvocation(CtInvocation<?> ctInvocation) {
        if (this.hasAccessedScanner(ctInvocation)) {
            this.getParentName(ctInvocation).ifPresent(name -> {
                if (!this.scannerInvocations.isEmpty() && !this.scannerInvocations.containsKey(name)) {
                    this.addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage(
                            "ui-input-separation",
                            Map.of(
                                "first",
                                SpoonUtil.formatSourcePosition(this.scannerInvocations.values().iterator().next())
                            )
                        ),
                        ProblemType.UI_INPUT_SEPARATION
                    );
                }

                this.scannerInvocations.put(name, ctInvocation.getPosition());
            });
        }

        if (this.hasAccessedSystem(ctInvocation)) {
            this.getParentName(ctInvocation).ifPresent(name -> {
                if (!this.systemInvocations.isEmpty() && !this.systemInvocations.containsKey(name)) {
                    this.addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage(
                            "ui-output-separation",
                            Map.of(
                                "first",
                                SpoonUtil.formatSourcePosition(this.systemInvocations.values().iterator().next())
                            )
                        ),
                        ProblemType.UI_OUTPUT_SEPARATION
                    );
                }

                this.systemInvocations.put(name, ctInvocation.getPosition());
            });
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                checkCtInvocation(ctInvocation);
            }
        });
    }
}
