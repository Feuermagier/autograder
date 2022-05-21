package edu.kit.informatik.model.track;

import edu.kit.informatik.cmd.CommandException;

/**
 * Represents any kind of error regarding he tracks
 *
 * @author ukidf
 * @version 1.0
 */
public class TrackException extends CommandException {
    /**
     * Creates a new TrackException
     * @param message An error message
     */
    public TrackException(String message) {
        super(message);
    }
}
