package edu.kit.informatik.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for parsing of user input. Maintains all registered commands and provides methods to execute commands
 *
 * @author ukidf
 * @version 1.0
 */
public class CommandResolver {

    private Map<Pattern, Command> commands = new HashMap<>();
    private boolean running = true;

    /**
     * Creates a new command resolver without any commands
     */
    public CommandResolver() {
    }

    /**
     * Adds a new command
     *
     * @param regex   The pattern of the command, should be created using CommandBuilder
     * @param command The command that should be executed if the user inputs a string matching the specified RegEx
     */
    public void register(String regex, Command command) {
        commands.put(Pattern.compile(regex), command);
    }

    /**
     * Looks for a command that matches the specified user input and executes it
     *
     * @param input The user input
     * @throws CommandException If there is no command matching the input or any other error happened during the
     *                          execution of the command
     */
    public void execute(String input) throws CommandException {

        for (Map.Entry<Pattern, Command> entry : commands.entrySet()) {
            Matcher matcher = entry.getKey().matcher(input);
            if (matcher.matches()) {
                running = entry.getValue().execute(matcher);
                return;
            }
        }

        throw new CommandException("Unknown Command");
    }

    /**
     * Checks if any command has stopped the app
     *
     * @return True if the app is still running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
