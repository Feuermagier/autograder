package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.check.Check;
import spoon.reflect.code.CtLabelledFlowBreak;

public class LabelProcessor extends AbstractLoggingProcessor<CtLabelledFlowBreak> {
    private static final String DESCRIPTION = "Used labels";
    private static final String EXPLANATION = """
            Labels enable a GOTO programming style in Java.
            Many people have written about the problems of GOTO, so PLEASE don't use labels.
            If you are still not convinced, google 'go to statements considered harmful'""";

    public LabelProcessor(Check check) {
        super(check);
    }

    @Override
    public void process(CtLabelledFlowBreak element) {
        if (element.getTargetLabel() != null) {
            addProblem(element, EXPLANATION);
        }
    }
}
