package de.firemage.autograder.extra.errorprone;

import de.firemage.autograder.api.TempLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * A class that can be used to run code in a new JVM.
 *
 * @param jvmArgs      arguments to pass to the new JVM (those are that java -... flags)
 * @param tempLocation a location where the result of the code can be written to (used for inter-process communication)
 */
public record VMLauncher(List<String> jvmArgs, TempLocation tempLocation, Optional<String> mainClassName) {
    public static VMLauncher fromDefault(TempLocation tmpLocation) throws IOException {
        Optional<String> mainClassName = Optional.empty();
        {
            String potentialName = System.getProperty("sun.java.command");

            if (potentialName != null) {
                String[] args = potentialName.split(" ");
                if (args.length > 0) {
                    mainClassName = Optional.of(args[0]);
                }
            }
        }

        return new VMLauncher(
            Arrays.asList(
                "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.source.tree=ALL-UNNAMED"
            ),
            tmpLocation.createTempDirectory("vm"),
            mainClassName
        );
    }

    private static String serialize(Serializable serializable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(serializable);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize object", e);
        }

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T deserialize(String string) {
        byte[] data = Base64.getDecoder().decode(string);
        try (ObjectInput objectInput = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (T) objectInput.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("Failed to deserialize object", e);
        }
    }

    /**
     * Runs the given lambda in a new JVM.
     * <p>
     * Note that the IDEs debugger will most likely not attach to code called in this lambda.
     * Therefore, it is recommended to run as little code as possible in this lambda.
     *
     * @param supplier the code to run in the new JVM
     * @param <T>      the type of the result returned by the lambda
     * @return a handle to the launched code
     * @throws IOException if it failed to serialize the lambda
     */
    // https://stackoverflow.com/a/65129876/7766117
    // NOTE: do not weaken the type to Serializable, it will not work!
    public <T extends Serializable> VMHandle<T> runInNewJVM(SerializableSupplier<T> supplier) throws IOException {
        ProcessHandle.Info currentProcessInfo = ProcessHandle.current().info();
        List<String> newProcessCommandLine = new ArrayList<>();
        newProcessCommandLine.add(currentProcessInfo.command().orElseThrow());

        newProcessCommandLine.add("-classpath");
        newProcessCommandLine.add(ManagementFactory.getRuntimeMXBean().getClassPath());


        // the result is written to a temporary file, because I could not find a way to do
        // inter-process communication (e.g. a channel to send back the result before exiting)
        Path resultFileLocation = this.tempLocation.createTempFile("result.txt");

        // inject custom jvm arguments:
        newProcessCommandLine.addAll(this.jvmArgs);
        // signal that it should launch our target class:
        newProcessCommandLine.add(TargetMain.class.getName());
        // provide the code that it should execute:
        newProcessCommandLine.add(serialize(supplier));
        // the result will be written to the file:
        newProcessCommandLine.add(resultFileLocation.toString());

        return new VMHandle<>(new ProcessBuilder(newProcessCommandLine), resultFileLocation);
    }

    /**
     * Represents a reference to the launched VM.
     *
     * @param <T> the type of the result returned by the launched lambda.
     */
    public static final class VMHandle<T extends Serializable> {
        private final Process process;
        private final Path resultFileLocation;
        private T value;

        private VMHandle(
            ProcessBuilder processBuilder,
            Path resultFileLocation
        ) throws IOException {
            this.process = processBuilder
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

            this.value = null;
            this.resultFileLocation = resultFileLocation;
        }

        private T getValue() throws IOException {
            if (this.value == null) {
                this.value = deserialize(Files.readString(this.resultFileLocation));
            }

            return this.value;
        }

        public T join() throws InterruptedException, IOException {
            int exitCode = this.process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Process exited with non-zero exit code: " + exitCode);
            }

            return this.getValue();
        }
    }

    private static final class TargetMain {
        private static void run(String[] args) {
            SerializableSupplier<? extends Serializable> supplier = deserialize(args[0]);

            try {
                Serializable value = supplier.get();
                Files.writeString(Path.of(args[1]), serialize(value));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to serialize supplier result", e);
            }
        }

        public static void main(String[] args) {
            try {
                run(args);

                System.exit(0);
            } catch (Exception exception) {
                exception.printStackTrace();

                System.exit(1);
            }
        }
    }
}
