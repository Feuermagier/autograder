package de.firemage.codelinter.executor;

import java.io.IOException;

public class Util {
    public static Process startJVM(String mainClass) throws IOException {
        String[] command = new String[] {
            "java",
            "-cp",
            "/home/student/studentcode.jar",
            "-javaagent:/home/student/Agent.jar",
            mainClass
        };
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        return Runtime.getRuntime().exec(command);
    }
}
