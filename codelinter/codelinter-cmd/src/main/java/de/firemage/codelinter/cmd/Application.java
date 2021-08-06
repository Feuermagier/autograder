package de.firemage.codelinter.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;

@Command(mixinStandardHelpOptions = true, version = "codelinter-cmd 1.0",
        description = "Static code analysis for student java code")
public class Application implements Runnable {
    @Parameters(index = "0", description = "The root folder which contains the files to check")
    private File file;

    @Option(names = {"-p", "--pmd"}, description = "enable PMD checks")
    private boolean enablePMD;

    @Option(names = {"-c", "--compile"}, description = "enable compilation")
    private boolean enableCompilation;

    @Option(names = {"-s", "--spotbugs"}, description = "enable SpotBugs checks")
    private boolean enableSpotBugs;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {

    }
}
