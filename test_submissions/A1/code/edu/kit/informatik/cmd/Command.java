package edu.kit.informatik.cmd;

import java.util.regex.MatchResult;

/**
 * Represents a simple command for use in lambda parameters to process command line input
 *
 * @author ukidf
 * @version 1.0
 */
@FunctionalInterface
public interface Command {
    /**
     * Execute this command
     *
     * @param input The result of parsing the line inputted by th user
     * @return True if the app should continue running, false otherwise
     * @throws CommandException If anything went wrong during the execution of this command
     */
    boolean execute(MatchResult input) throws CommandException;
}
