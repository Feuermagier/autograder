package de.firemage.autograder.agent;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public class AnalysisAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        EventRecorder.setOutPath(Path.of("/home/student/codelinter_events.txt"));
        instrumentation.addTransformer(new ClassTransformer());
    }
}
