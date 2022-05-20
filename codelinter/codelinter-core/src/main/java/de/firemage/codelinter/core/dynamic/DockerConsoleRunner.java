package de.firemage.codelinter.core.dynamic;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import de.firemage.codelinter.event.Event;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DockerConsoleRunner implements TestRunner {
    private static final int TIMEOUT_SECONDS = 60;
    private final Path tmpDirectory;
    private final Path executor;
    private final Path agent;
    private final Path tests;

    public DockerConsoleRunner(Path tmpDirectory, Path executor, Path agent, Path tests) {
        this.tmpDirectory = tmpDirectory;
        this.executor = executor;
        this.agent = agent;
        if (!Files.isDirectory(tests)) {
            throw new IllegalArgumentException("tests must point to a folder containing the individual test cases");
        }
        this.tests = tests;
    }

    public List<List<Event>> runTests(StaticAnalysis analysis, Path jar) throws IOException, InterruptedException {
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

        List<Path> testCases;
        try (Stream<Path> files = Files.list(this.tests)) {
            testCases = files.filter(Files::isRegularFile).toList();
        }

        List<List<Event>> events = new ArrayList<>();
        for (Path testPath : testCases) {
            events.add(executeTestCase(dockerClient, imageId, testPath, mainClass, jar));
        }

        return events;
    }

    private List<Event> executeTestCase(DockerClient dockerClient, String imageId, Path testFile, String mainClass,
                                        Path jar)
        throws IOException {

        List<String> interactionLines = Files.readAllLines(testFile);
        String containerId = dockerClient
            .createContainerCmd(imageId)
            .withCmd(mainClass, Base64.getEncoder().encodeToString(String.join("\n", interactionLines).getBytes(StandardCharsets.UTF_8)))
            .exec()
            .getId();
        dockerClient.startContainerCmd(containerId).exec();

        int exitCode;
        try {
            exitCode = dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback())
                .awaitStatusCode(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (DockerClientException ex) {
            dockerClient.stopContainerCmd(containerId).exec();
            throw new IllegalStateException("The test container did not exit");
        }

        if (exitCode != 0) {
            throw new IllegalStateException("The container did not exit with exit code 0");
        }

        InputStream eventsStream =
            dockerClient.copyArchiveFromContainerCmd(containerId, "/home/student/codelinter_events.txt").exec();
        List<Event> events = Event.read(eventsStream);

        dockerClient.removeContainerCmd(containerId).withForce(true).withRemoveVolumes(true).exec();

        return events;
    }
}
