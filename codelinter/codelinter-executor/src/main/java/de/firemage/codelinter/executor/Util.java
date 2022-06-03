package de.firemage.codelinter.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static Process startJVM(String mainClass, List<String> args) throws IOException {
        List<String> command = new ArrayList<>(List.of(
            "java",
            "-cp",
            "/home/student/studentcode.jar",
            "-javaagent:/home/student/Agent.jar",
            mainClass
        ));
        command.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        return builder.start();
    }
}
