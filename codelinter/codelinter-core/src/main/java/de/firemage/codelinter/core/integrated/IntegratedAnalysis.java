package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.dynamic.DockerConsoleRunner;
import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.core.spoon.CompilationException;
import de.firemage.codelinter.core.spoon.SpoonCheck;
import de.firemage.codelinter.core.spoon.check.CodeProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IntegratedAnalysis implements AutoCloseable {
    private final Path jar;
    private final StaticAnalysis staticAnalysis;
    private final Path tmpLocation;
    private DynamicAnalysis dynamicAnalysis;

    public IntegratedAnalysis(UploadedFile file, Path jar, Path tmpLocation) throws CompilationException, IOException {
        this.jar = jar;
        this.tmpLocation = tmpLocation;

        this.staticAnalysis = new StaticAnalysis(file, jar);
    }

    public void runDynamicAnalysis() throws IOException, InterruptedException {
        DockerConsoleRunner runner = new DockerConsoleRunner(this.tmpLocation,
            Path.of("codelinter-executor/target/codelinter-executor-1.0-SNAPSHOT.jar"),
            Path.of("codelinter-agent/target/codelinter-agent-1.0-SNAPSHOT.jar"),
            Path.of("tests"));
        var events = runner.runTests(this.staticAnalysis, this.jar);
        this.dynamicAnalysis = new DynamicAnalysis(events);
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

    public StaticAnalysis getStaticAnalysis() {
        return staticAnalysis;
    }

    public DynamicAnalysis getDynamicAnalysis() {
        return dynamicAnalysis;
    }

    @Override
    public void close() throws IOException {
        this.staticAnalysis.close();
    }
}
