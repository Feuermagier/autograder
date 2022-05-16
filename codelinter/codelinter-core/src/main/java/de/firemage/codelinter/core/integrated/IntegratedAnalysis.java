package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.dynamic.ConsoleRunner;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonCheck;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;
import de.firemage.codelinter.event.Event;
import spoon.reflect.declaration.CtMethod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IntegratedAnalysis implements AutoCloseable {
    private final Path jar;
    private final StaticAnalysis staticAnalysis;
    private List<List<Event>> events = new ArrayList<>();
    private Path tmpLocation;

    public IntegratedAnalysis(UploadedFile file, Path jar, Path tmpLocation) throws CompilationException, IOException {
        this.jar = jar;
        this.tmpLocation = tmpLocation;

        this.staticAnalysis = new StaticAnalysis(file, jar);
    }

    public void runDynamicAnalysis() throws IOException, InterruptedException {
        ConsoleRunner runner = new ConsoleRunner(this.tmpLocation,
            Path.of("codelinter-executor/target/codelinter-executor-1.0-SNAPSHOT.jar"), Path.of("tests"));
        this.events = runner.runTests(this.staticAnalysis, this.jar);
    }

    public List<Problem> lint(List<SpoonCheck> checks) {
        //MethodAnalysis methodAnalysis = new MethodAnalysis(this.model);
        //methodAnalysis.run();

        List<Problem> problems = new ArrayList<>();
        for (SpoonCheck check : checks) {
            CodeProcessor processor = check.getProcessor().get();
            problems.addAll(processor.check(this.staticAnalysis.getModel(), this.staticAnalysis.getFactory()));
        }

        return problems;
    }

    @Override
    public void close() throws IOException {
        this.staticAnalysis.close();
    }
}
