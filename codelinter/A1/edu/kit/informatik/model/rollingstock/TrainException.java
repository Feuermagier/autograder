package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.cmd.CommandException;

/**
 * Represents any kind of error regarding rolling stock, complete trains or the movement of trains
 *
 * @author ukidf
 * @version 1.0
 */
public class TrainException extends CommandException {
    /**
     * Creates a new TrainException
     * @param message   An error message
     */
    public TrainException(String message) {
        super(message);
    }
}
