package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.reference.CtTypeReference;

@ExecutableCheck(reportedProblems = {ProblemType.SCANNER_MUST_BE_CLOSED})
public class ScannerClosedCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall<?> ctConstructorCall) {
                CtTypeReference<?> ctTypeReference = ctConstructorCall.getType();
                if (ctTypeReference == null) return;

                if (ctConstructorCall.isImplicit() || !ctConstructorCall.getPosition().isValidPosition()) return;

                CtTypeReference<?> scannerType = ctConstructorCall.getFactory().Type().createReference(java.util.Scanner.class);
                if (!ctTypeReference.equals(scannerType)) {
                    return;
                }

                boolean isClosed =
                    staticAnalysis.getModel().filterChildren(ctElement -> ctElement instanceof CtInvocation<?> ctInvocation
                    && ctInvocation.getExecutable() != null
                    && ctInvocation.getExecutable().getSimpleName().equals("close")
                    && ctInvocation.getTarget() != null
                    && scannerType.equals(ctInvocation.getTarget().getType())
                ).first() != null;

                if (!isClosed && ctConstructorCall.getParent(CtTryWithResource.class) == null) {
                    addLocalProblem(
                        ctConstructorCall,
                        new LocalizedMessage("scanner-closed"),
                        ProblemType.SCANNER_MUST_BE_CLOSED
                    );
                }
            }
        });
    }
}
