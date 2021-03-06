package de.firemage.autograder.executor;

import java.io.IOException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String main = args[0];

        if (args.length == 3) {
            String test = new String(Base64.getDecoder().decode(args[1]));
            System.out.println("=============== Running a console test ==================");
            new ConsoleExecutor().execute(main, test, Boolean.parseBoolean(args[2]));
        }
    }
}
