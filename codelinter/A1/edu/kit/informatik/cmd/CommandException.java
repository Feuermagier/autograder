package edu.kit.informatik.cmd;

/**
 * Represents any type of error that occurred during parsing of user input
 *
 * @author ukidf
 * @version 1.0
 */
public class CommandException extends Exception {
    /**
     * Constructs a new CommandException containing the specified error string
     *
     * @param message The error string
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new CommandException containing the specified error string and the specified cause
     *
     * @param message The error string
     * @param cause   The cause of this exception
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
