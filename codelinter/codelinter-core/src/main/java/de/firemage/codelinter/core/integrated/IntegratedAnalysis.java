package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.dynamic.DockerConsoleRunner;
import de.firemage.codelinter.core.dynamic.DockerRunnerException;
import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.dynamic.TestRunResult;
import de.firemage.codelinter.core.file.UploadedFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IntegratedAnalysis implements AutoCloseable {
    private final UploadedFile file;
    private final Path jar;
    private final StaticAnalysis staticAnalysis;
    private DynamicAnalysis dynamicAnalysis;

    public IntegratedAnalysis(UploadedFile file, Path jar, Consumer<String> statusConsumer) throws ModelBuildException, IOException {
        this.file = file;
        this.jar = jar;

        this.staticAnalysis = new StaticAnalysis(file, jar, statusConsumer);
    }

    public void runDynamicAnalysis(Path tests, Consumer<String> statusConsumer) throws IOException, InterruptedException, DockerRunnerException {
        DockerConsoleRunner runner = new DockerConsoleRunner(
                Path.of("codelinter-executor/target/codelinter-executor-1.0-SNAPSHOT.jar"),
                Path.of("codelinter-agent/target/codelinter-agent-1.0-SNAPSHOT.jar"),
                tests);
        List<TestRunResult> results = runner.runTests(this.staticAnalysis, this.jar, statusConsumer);
        this.dynamicAnalysis = new DynamicAnalysis(results);
    }

    public List<Problem> lint(List<IntegratedCheck> checks) {
        //MethodAnalysis methodAnalysis = new MethodAnalysis(this.model);
        //methodAnalysis.run();

        List<Problem> problems = new ArrayList<>();
        for (IntegratedCheck check : checks) {
            problems.addAll(check.run(this.staticAnalysis, this.dynamicAnalysis, this.file.getFile()));
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
