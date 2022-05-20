package de.firemage.codelinter.agent;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public class AnalysisAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        EventRecorder.setOutPath(Path.of("codelinter_events.txt"));
        instrumentation.addTransformer(new ClassTransformer());
    }
}
