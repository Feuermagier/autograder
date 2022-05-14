package de.firemage.codelinter.executor;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public class AnalysisAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        EventRecorder.setOutPath(Path.of(args));
        instrumentation.addTransformer(new ClassTransformer());
    }
}
