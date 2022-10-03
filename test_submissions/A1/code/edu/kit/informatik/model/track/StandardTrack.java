package edu.kit.informatik.model.track;

import edu.kit.informatik.StringConstants;
import edu.kit.informatik.model.Vector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a simple track with one starting point and one end point
 * <p>
 * Also stores up to three other tracks which are connected to the start point or the ending points.
 * Important: This class doesn't check if the following tracks can be connected to this track.
 *
 * @author uxxxx
 * @version 1.0
 */
public class StandardTrack extends Track {

    private static final String PREFIX = "t";

    private final Vector start;
    private final Vector end;
    private final Vector direction;

    private Track nextTrack = null;
    private Track previousTrack = null;

    /**
     * Creates a new standard (straight) track
     *
     * @param start The starting point of this track
     * @param end   The end of this track
     * @throws TrackException If the start and end point are the same or the track is not horizontal / vertical
     */
    public StandardTrack(Vector start, Vector end) throws TrackException {
        super();

        if (start.equals(end)) {
            remove();
            throw new TrackException("Start and end point must be different");
        }

        // Calculate the vector between start and end to check the direction of this track
        Vector difference = end.subtract(start);
        if (difference.getX() != 0 && difference.getY() != 0) {
            remove();
            throw new TrackException("The track must be either horizontal or vertical");
        }
        direction = difference.normalize();

        this.start = start;
        this.end = end;
    }

    @Override
    public int getLength() {
        return start.subtract(end).length();
    }

    @Override
    public void setConnectedTrack(Vector point, Track track) {
        if (point.equals(start)) {
            previousTrack = track;
        } else if (point.equals(end)) {
            nextTrack = track;
        } else {
            throw new IllegalArgumentException("One of the tracks doesn't have the given point as an end point");
        }
    }

    @Override
    public Vector getOtherActiveEnd(Vector point) throws IllegalArgumentException {
        if (point.equals(start)) {
            return end;
        } else if (point.equals(end)) {
            return start;
        } else {
            throw new IllegalArgumentException("The given point is neither the starting point nor the end point");
        }
    }

    @Override
    public List<Vector> getAllPoints() {
        return Arrays.asList(start, end);
    }

    @Override
    public Track findConnectedTrack(Vector point) {
        if (point.equals(start)) {
            return previousTrack;
        } else if (point.equals(end)) {
            return nextTrack;
        } else {
            throw new IllegalArgumentException("The given point isn't an end point of this track");
        }
    }

    @Override
    public List<Track> getAllConnectedTracks() {
        List<Track> tracks = new LinkedList<>(Arrays.asList(previousTrack, nextTrack));
        tracks.removeIf(Objects::isNull);
        return tracks;
    }

    @Override
    public Vector getDirection() {
        return direction;
    }

    /**
     * As you can not set the direction of a standard track, this will throw an exception
     *
     * @param direction The new direction (wil be ignored)
     * @throws TrackException Will always be thrown
     */
    @Override
    public void setDirection(Vector direction) throws TrackException {
        throw new TrackException("You cannot set the direction of a normal track");
    }

    @Override
    public Vector getNextPointInDirection(Vector point, Vector direction) {
        if (point.equals(start) && direction.equals(this.direction)) {
            return end;
        } else if (point.equals(end) && direction.equals(this.direction.inverted())) {
            return start;
        } else {
            return null;
        }
    }

    @Override
    public Vector getFirstActiveEnd() throws TrackException {
        return start;
    }

    @Override
    public Vector getSecondActiveEnd() throws TrackException {
        return end;
    }

    @Override
    public void removeConnectedTrack(Track track) {
        if (track.equals(previousTrack)) {
            previousTrack = null;
        } else if (track.equals(nextTrack)) {
            nextTrack = null;
        } else {
            throw new IllegalArgumentException("The track is not connected to this track");
        }
    }

    @Override
    public String toString() {
        return PREFIX + StringConstants.SPACE
            + super.getId() + StringConstants.SPACE
            + start + StringConstants.SPACE + StringConstants.ARROW + StringConstants.SPACE
            + end + StringConstants.SPACE
            + getLength();
    }
}
