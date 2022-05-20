package de.firemage.codelinter.core.dynamic;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import de.firemage.codelinter.event.Event;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import spoon.reflect.declaration.CtClass;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DockerConsoleRunner implements TestRunner {
    private static final int TIMEOUT_SECONDS = 60;
    private final Path executor;
    private final Path agent;
    private final Path tests;

    public DockerConsoleRunner(Path executor, Path agent, Path tests) {
        this.executor = executor;
        this.agent = agent;
        if (!Files.isDirectory(tests)) {
            throw new IllegalArgumentException("tests must point to a folder containing the individual test cases");
        }
        this.tests = tests;
    }

    public List<TestRunResult> runTests(StaticAnalysis analysis, Path jar) throws IOException, InterruptedException, DockerRunnerException {
        String mainClass = analysis.findMain().getParent(CtClass.class).getQualifiedName().replace(".", "/");

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient client = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerHttpClient(client).build();
        String imageId = dockerClient.buildImageCmd()
                .withBaseDirectory(new File("."))
                .withDockerfile(new File("Dockerfile"))
                .withPull(true)
                .withBuildArg("jarfile", Paths.get("").relativize(jar).toString().replace("\\", "/"))
                .withBuildArg("executor", Paths.get("").relativize(this.executor).toString().replace("\\", "/"))
                .withBuildArg("agent", Paths.get("").relativize(this.agent).toString().replace("\\", "/"))
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        try {

            List<Path> testCases;
            try (Stream<Path> files = Files.list(this.tests)) {
                testCases = files.filter(Files::isRegularFile).toList();
            }

            List<TestRunResult> results = new ArrayList<>();
            for (Path testPath : testCases) {
                results.add(executeTestCase(dockerClient, imageId, testPath, mainClass));
            }

            return results;
        } finally {
            dockerClient.removeImageCmd(imageId).exec();
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
                .withCmd(mainClass, Base64.getEncoder().encodeToString(String.join("\n", interactionLines).getBytes(StandardCharsets.UTF_8)))
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

            InputStream tar = dockerClient
                    .copyArchiveFromContainerCmd(containerId, "/home/student/codelinter_events.txt")
                    .exec();
            List<Event> events = Event.read(extractSingleFileFromTar(tar));

            return new TestRunResult(events, status, readLogs(dockerClient, containerId));
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
