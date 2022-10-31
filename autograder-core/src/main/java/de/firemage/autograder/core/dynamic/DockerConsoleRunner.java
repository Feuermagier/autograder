package de.firemage.autograder.core.dynamic;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.event.Event;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DockerConsoleRunner implements TestRunner {
    private static final int TIMEOUT_SECONDS = 60;
    private final Path executor;
    private final Path agent;
    private final Path tests;
    private final Path tmpPath;

    public DockerConsoleRunner(Path executor, Path agent, Path tests, Path tmpPath) {
        this.executor = executor;
        this.agent = agent;
        if (!Files.isDirectory(tests)) {
            throw new IllegalArgumentException("tests must point to a folder containing the individual test cases");
        }
        this.tests = tests;
        this.tmpPath = tmpPath;
    }

    public List<TestRunResult> runTests(StaticAnalysis analysis, Path jar, Consumer<LinterStatus> statusConsumer)
        throws RunnerException, InterruptedException {
        String mainClass = analysis.findMain().getParent(CtClass.class).getQualifiedName().replace(".", "/");

        statusConsumer.accept(LinterStatus.BUILDING_DOCKER_IMAGE);

        // Create the docker client
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient client =
            new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig())
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerHttpClient(client).build();

        String imageId;
        List<Path> testCases;
        try {
            // Prepare the build directory
            Path buildDirectory = Files.createTempDirectory(this.tmpPath, "docker_build");
            Files.copy(Path.of(this.getClass().getResource("Dockerfile").toURI()),
                buildDirectory.resolve("Dockerfile"));
            Files.copy(this.executor, buildDirectory.resolve("executor.jar"));
            Files.copy(this.agent, buildDirectory.resolve("agent.jar"));
            Files.copy(jar, buildDirectory.resolve("src.jar"));

            imageId = dockerClient.buildImageCmd()
                .withBaseDirectory(buildDirectory.toFile())
                .withDockerfile(buildDirectory.resolve("Dockerfile").toFile())
                .withPull(true)
                .withBuildArg("jarfile", "src.jar")
                .withBuildArg("executor", "executor.jar")
                .withBuildArg("agent", "agent.jar")
                .exec(new BuildImageResultCallback())
                .awaitImageId();

            // Clean up the build directory
            try (Stream<Path> walk = Files.walk(buildDirectory)) {
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }

            try (Stream<Path> files = Files.walk(this.tests)) {
                testCases =
                    files.filter(Files::isRegularFile)
                        .filter(f -> f.toString().endsWith(".txt") || f.toString().endsWith(".protocol")).toList();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RunnerException(e);
        }


        statusConsumer.accept(LinterStatus.EXECUTING_TESTS);
        try {
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<TestRunResult>> futures = new ArrayList<>();
            for (Path testPath : testCases) {
                futures.add(service.submit(() -> executeTestCase(dockerClient, imageId, testPath, mainClass)));
            }
            List<TestRunResult> results = new ArrayList<>();
            for (var future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    throw new RunnerException(e.getCause());
                }
            }
            service.shutdown();

            System.out.println(System.lineSeparator());
            System.out.println(
                results.stream().filter(t -> t.status() == TestRunResult.TestRunStatus.OK).count() + "/" +
                    results.size() + " tests successful");

            return results;
        } finally {
            dockerClient.removeImageCmd(imageId).withForce(true).exec();
        }
    }

    private TestRunResult executeTestCase(DockerClient dockerClient, String imageId, Path testFile, String mainClass)
        throws IOException, InterruptedException, DockerRunnerException {

        List<String> interactionLines = Files.readAllLines(testFile);
        String containerId = dockerClient
            .createContainerCmd(imageId)
            .withHostConfig(
                new HostConfig()
                    .withCapDrop()
                    .withNetworkMode("none")
                    .withPidsLimit(2000L)
                    .withMemory(200 * (1L << 20))
            )
            .withCmd(mainClass, Base64.getEncoder()
                    .encodeToString(String.join("\n", interactionLines).getBytes(StandardCharsets.UTF_8)),
                String.valueOf(false))
            .exec()
            .getId();

        try {
            dockerClient.startContainerCmd(containerId).exec();

            TestRunResult.TestRunStatus status = TestRunResult.TestRunStatus.OK;
            int exitCode;
            try {
                exitCode = dockerClient
                    .waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (DockerClientException ex) {
                throw new DockerRunnerException("The test container timed out", readLogs(dockerClient, containerId));
            }

            if (exitCode == 1) {
                status = TestRunResult.TestRunStatus.ERROR_TEST_FAILURE;
            } else if (exitCode == 2) {
                throw new DockerRunnerException("The executor failed", readLogs(dockerClient, containerId));
            }

            String logs = readLogs(dockerClient, containerId);

            List<Event> events;
            try {
                InputStream tar = dockerClient
                    .copyArchiveFromContainerCmd(containerId, "/home/student/codelinter_events.txt")
                    .exec();
                events = Event.read(extractSingleFileFromTar(tar));
            } catch (NotFoundException ex) {
                events = List.of();
            }

            synchronized (this) {
                System.out.println(System.lineSeparator());
                System.out.println(logs);
                if (events.isEmpty()) {
                    System.err.println("No events found. Maybe the student's code timed out.");
                    System.err.flush();
                }
            }

            return new TestRunResult(events, status, logs);
        } finally {
            dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
        }
    }

    private InputStream extractSingleFileFromTar(InputStream tar) throws IOException {
        TarArchiveInputStream tarStream = new TarArchiveInputStream(tar);
        if (tarStream.getNextEntry() == null) {
            throw new IllegalArgumentException("No file found inside the tar file");
        }
        return tarStream;
    }

    private String readLogs(DockerClient client, String containerId) throws InterruptedException {
        StringBuilder log = new StringBuilder();
        client.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame frame) {
                    log.append(frame);
                    log.append(System.lineSeparator());
                }
            }).awaitCompletion();
        return log.toString();
    }
}
