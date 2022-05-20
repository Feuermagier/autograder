package de.firemage.codelinter.executor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleExecutor {
    public void execute(String mainClass, String test) throws IOException, InterruptedException {
        Process process = Util.startJVM(mainClass);

        OutputStream containerIn = process.getOutputStream();

        Queue<String> containerOut = new ConcurrentLinkedQueue<>();
        Thread outThread = new Thread(new ProcessReader(containerOut, process.getInputStream()));
        outThread.setDaemon(true);
        outThread.start();

        String[] lines = test.split("\n");
        for (String line : lines) {
            if (line.startsWith("> ")) {
                System.out.println("IN:    " + line.substring(2) );
                containerIn.write(line.substring(2).getBytes());
                containerIn.write("\n".getBytes());
                containerIn.flush();
            } else {
                String output = pollOutput(containerOut);
                if (!matchOutput(output, line)) {
                    System.exit(1);
                }
            }
        }

        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            System.err.println("The child JVM did not exit after 5s");
            System.exit(1);
        }

        if (process.exitValue() != 0) {
            System.err.println("The child JVM did not exit with exit code 0");
            System.exit(1);
        }
    }

    private boolean matchOutput(String output, String line) {
        if (line.equals(output) || (line.startsWith("Error,") && output.startsWith("Error,"))) {
            return true;
        } else {
            System.err.println("Invalid output, got '" + output + "', expected '" + line + "' ");
            return false;
        }
    }

    private String pollOutput(Queue<String> queue) {
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
                System.err.println("Did not receive any output after 5s");
                System.exit(1);
            }
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
