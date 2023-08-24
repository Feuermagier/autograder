package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.dynamic.DockerConsoleRunner;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.dynamic.RunnerException;
import de.firemage.autograder.core.dynamic.TestRunResult;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.graph.GraphAnalysis;
import de.firemage.autograder.core.parallel.AnalysisScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class IntegratedAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(IntegratedAnalysis.class);

    private final UploadedFile file;
    private final Path tmpPath;
    private final Map<String, FileSystem> openFileSystems = new HashMap<>();
    private final StaticAnalysis staticAnalysis;
    private final GraphAnalysis graphAnalysis;
    private DynamicAnalysis dynamicAnalysis;

    public IntegratedAnalysis(UploadedFile file, Path tmpPath) {
        this.file = file;
        this.tmpPath = tmpPath;

        this.staticAnalysis = new StaticAnalysis(file.getModel(), file.getCompilationResult());
        //this.graphAnalysis = new GraphAnalysis(this.staticAnalysis.getCodeModel());
        this.graphAnalysis = null; //TODO
        this.dynamicAnalysis = new DynamicAnalysis(List.of());

    }

    public void runDynamicAnalysis(Path tests, Consumer<LinterStatus> statusConsumer)
        throws RunnerException, InterruptedException {
        try {
            DockerConsoleRunner runner = new DockerConsoleRunner(toPath(this.getClass().getResource("/executor.jar")),
                toPath(this.getClass().getResource("/agent.jar")), tests, this.tmpPath);
            List<TestRunResult> results =
                runner.runTests(this.staticAnalysis, this.file.getCompilationResult().jar(), statusConsumer);
            this.dynamicAnalysis = new DynamicAnalysis(results);
        } catch (URISyntaxException | IOException e) {
            throw new RunnerException(e);
        } finally {
            closeOpenFileSystems();
        }
    }

    private Path toPath(URL resource) throws URISyntaxException, IOException {
        if (resource == null) {
            throw new IllegalArgumentException("URL is null");
        }
        URI uri = resource.toURI();
        if (!uri.toString().contains("!")) {
            return Path.of(uri);
        }
        // See https://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
        String[] path = uri.toString().split("!", 2);
        @SuppressWarnings("resource") FileSystem fs = createFileSystem(path[0]);
        return fs.getPath(path[1]);
    }

    private FileSystem createFileSystem(String path) throws IOException {
        FileSystem existingFileSystem = openFileSystems.get(path);
        if (existingFileSystem != null) {
            return existingFileSystem;
        }

        FileSystem newFileSystem = FileSystems.newFileSystem(URI.create(path), new HashMap<>());
        openFileSystems.put(path, newFileSystem);
        return newFileSystem;
    }

    private void closeOpenFileSystems() {
        for (FileSystem openFileSystem : openFileSystems.values()) {
            try {
                openFileSystem.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        openFileSystems.clear();
    }

    public void lint(List<IntegratedCheck> checks, Consumer<LinterStatus> statusConsumer, AnalysisScheduler scheduler) {
        statusConsumer.accept(LinterStatus.BUILDING_CODE_MODEL);
        this.staticAnalysis.getCodeModel().ensureModelBuild();

        statusConsumer.accept(LinterStatus.RUNNING_INTEGRATED_CHECKS);

        // NOTE: If the autograder has some randomly occurring issues, this might be the cause:
        // Execute checks in sequence to avoid race conditions in spoon.
        // Some queries on the spoon model have writes that are not synchronized.
        //
        // This has caused a crash, where one queried if a type (from java.lang) is a subtype of another type,
        // which would invoke the shadow model. This is built lazily, and seems to cause a race-condition.
        for (IntegratedCheck check : checks) {
            scheduler.submitTask((s, reporter) -> {
                long beforeTime = System.nanoTime();
                reporter.reportProblems(check.run(
                    this.staticAnalysis,
                    this.dynamicAnalysis,
                    this.file.getSource()
                ));
                long afterTime = System.nanoTime();
                logger.info("Completed check " + check.getClass().getSimpleName() + " in " + ((afterTime - beforeTime) / 1_000_000 + "ms"));
            });
        }
    }

    public StaticAnalysis getStaticAnalysis() {
        return staticAnalysis;
    }

    public DynamicAnalysis getDynamicAnalysis() {
        return dynamicAnalysis;
    }
}
