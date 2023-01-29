package de.firemage.autograder.executor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleExecutor {
    public void execute(String mainClass, String test, boolean quitOnFailure) throws IOException, InterruptedException {
        Iterator<String> lines = Arrays.stream(test.split("\n")).iterator();
        List<String> args = parseHeader(lines);
        if (executeTest(args, mainClass, lines, quitOnFailure)) {
            System.err.println("EXEC:  Test success");
        } else {
            System.err.println("EXEC:  Test failure");
        }
    }

    private List<String> parseHeader(Iterator<String> lines) {
        List<String> args = null;
        while (lines.hasNext()) {
            String line = lines.next();
            if (line.startsWith("--")) {
                return args;
            } else if (line.startsWith("name:")) {
                System.out.println("EXEC:  Running test " + line.substring(6));
            } else if (line.startsWith("comment:")) {
                System.out.println("EXEC:  " + line.substring(9));
            } else if (line.startsWith("args:")) {
                if (line.length() <= 6) {
                    args = List.of();
                } else {
                    args = List.of(line.substring(6).split(" "));
                }
            }
        }
        throw new IllegalStateException("Invalid test file: end of header missing");
    }

    private boolean executeTest(List<String> args, String mainClass, Iterator<String> lines, boolean quitOnFailure)
        throws IOException, InterruptedException {

        Process process = Util.startJVM(mainClass, args);

        OutputStream containerIn = process.getOutputStream();

        Queue<String> containerOut = new ConcurrentLinkedQueue<>();
        Thread outThread = new Thread(new ProcessReader(containerOut, process.getInputStream()));
        outThread.setDaemon(true);
        outThread.start();

        boolean failed = false;
        while (lines.hasNext()) {
            String line = lines.next();
            if (line.startsWith("###")) {
                // Comment - ignore
            } else if (line.startsWith(">")) {
                // Input
                pollAllOutput(containerOut);
                System.out.println("IN:    " + line.substring(1));
                containerIn.write(line.substring(1).getBytes());
                containerIn.write("\n".getBytes());
                containerIn.flush();
            } else {
                // Expected output
                String output = pollOutput(process, containerOut);
                if (output == null) {
                    pollAllOutput(containerOut);
                    System.err.println("EXEC:  The child JVM exited unexpectedly");
                    return false;
                }
                if (!matchOutput(output, line)) {
                    failed = true;
                    if (quitOnFailure) {
                        killVM(process);
                        pollAllOutput(containerOut);
                        return false;
                    }

                }
            }
        }

        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            System.err.println("EXEC:  The child JVM did not exit after 5s");
            killVM(process);
            pollAllOutput(containerOut);
            return false;
        }

        pollAllOutput(containerOut);

        System.out.println("EXEC:  Child JVM exited");

        if (process.exitValue() != 0) {
            System.err.println("EXEC:  The child JVM did not exit with exit code 0");
            return false;
        }

        return !failed;
    }

    private boolean matchOutput(String output, String line) {
        if (line.equals(output) ||
            (line.equals("!A!!R!^(E|e)rror.*") && (output.startsWith("Error") || output.startsWith("error")))) {
            return true;
        } else {
            System.err.println("EXEC:  Invalid output, got '" + output + "', expected '" + line + "' ");
            return false;
        }
    }

    private String pollOutput(Process process, Queue<String> queue) throws InterruptedException {
        long beforeTime = System.currentTimeMillis();
        while (true) {
            if (!queue.isEmpty()) {
                String result = queue.poll();
                if (result.startsWith("AGENT")) {
                    System.out.println(result);
                    continue;
                }
                System.out.println("OUT:   " + result);
                return result;
            }
            if (System.currentTimeMillis() - beforeTime > 5000) {
                System.err.println("EXEC:  Did not receive any output after 5s");
                return null;
            }
            if (!process.isAlive()) {
                return null;
            }
            Thread.sleep(10);
        }
    }

    private void killVM(Process process) throws InterruptedException {
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
    }

    private void pollAllOutput(Queue<String> containerOut) {
        while (!containerOut.isEmpty()) {
            System.out.println("OUT:   " + containerOut.poll());
        }
    }

    private static class ProcessReader implements Runnable {
        private final Scanner scanner;
        private final Queue<String> queue;

        private ProcessReader(Queue<String> queue, InputStream inputStream) {
            this.queue = queue;
            this.scanner = new Scanner(inputStream);
        }

        @Override
        public void run() {
            while (this.scanner.hasNextLine()) {
                this.queue.add(this.scanner.nextLine());
            }
        }
    }
}
