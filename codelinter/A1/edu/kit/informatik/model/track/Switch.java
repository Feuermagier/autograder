package edu.kit.informatik.model.track;

import edu.kit.informatik.StringConstants;
import edu.kit.informatik.model.Vector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single switch consisting of one starting point and to end points (the straight and
 * the diverging end). Straight and diverging end are interchangeable,
 * i.e. the name doesn't make any suggestion on the direction of the track.
 * After creation the direction of this switch is undefined and has to be set before using it in
 * simulation.
 * <p>
 * Also stores up to three other tracks which are connected to the start point or the ending points.
 *
 * @author ukidf
 * @version 1.0
 */
public class Switch extends Track {

    private static final String PREFIX = "s";

    private final Vector start;
    private final Vector straightEnd;
    private final Vector divergingEnd;

    private Vector currentPosition = null;
    private Vector currentDirection = null;

    private Track previousTrack = null;
    private Track nextStraightTrack = null;
    private Track nextDivergingTrack = null;

    /**
     * Creates a new switch
     *
     * @param start        The starting point of this switch
     * @param straightEnd  The straight end of this switch
     * @param divergingEnd The diverging end of this switch
     * @throws TrackException If the switch is not horizontal / vertical or the end points are the same
     */
    public Switch(Vector start, Vector straightEnd, Vector divergingEnd) throws TrackException {
        super();

        if (start.equals(straightEnd) || start.equals(divergingEnd) || straightEnd.equals(divergingEnd)) {
            remove();
            throw new TrackException("The end points must be different");
        }

        // Calculate the vector between start and the end points to check the directions of this switch
        Vector straightDifference = start.subtract(straightEnd);
        Vector divergingDifference = start.subtract(divergingEnd);
        if (!((straightDifference.getX() == 0 && divergingDifference.getY() == 0)
            || (straightDifference.getY() == 0 && divergingDifference.getX() == 0))) {

            remove();
            throw new TrackException("The track must be either horizontal or vertical");
        }

        this.start = start;
        this.straightEnd = straightEnd;
        this.divergingEnd = divergingEnd;
    }

    /**
     * Checks if the position of this switch has already been set
     *
     * @return True if the position has already set, false otherwise
     */
    public boolean isCurrentPositionSet() {
        return currentPosition != null;
    }

    @Override
    public int getLength() throws TrackException {
        if (currentPosition != null) {
            return start.subtract(currentPosition).length();
        } else {
            throw new TrackException("You haven't set a direction for this switch");
        }
    }

    @Override
    public void setConnectedTrack(Vector point, Track track) {
        if (point.equals(start)) {
            previousTrack = track;
        } else if (point.equals(straightEnd)) {
            nextStraightTrack = track;

        } else if (point.equals(divergingEnd)) {
            nextDivergingTrack = track;
        } else {
            throw new IllegalArgumentException("This track doesn't have the given point as an end point");
        }
    }

    @Override
    public Vector getOtherActiveEnd(Vector point) throws IllegalArgumentException, TrackException {
        if (!isCurrentPositionSet()) {
            throw new TrackException("The position of the switch hasn't been set yet");
        }

        if (point.equals(straightEnd) || point.equals(divergingEnd)) {
            return start;
        } else if (point.equals(start)) {
            return currentPosition;
        } else {
            throw new IllegalArgumentException("The given point is neither the starting nor an end point");
        }
    }

    @Override
    public Vector getFirstActiveEnd() throws TrackException {
        return start;
    }

    @Override
    public Vector getSecondActiveEnd() throws TrackException {
        return currentPosition;
    }

    @Override
    public List<Vector> getAllPoints() {
        return Arrays.asList(start, straightEnd, divergingEnd);
    }

    @Override
    public Track findConnectedTrack(Vector point) {
        if (point.equals(start)) {
            return previousTrack;
        } else if (point.equals(straightEnd)) {
            return nextStraightTrack;
        } else if (point.equals(divergingEnd)) {
            return nextDivergingTrack;
        } else {
            throw new IllegalArgumentException("The given point isn't an end point of this track");
        }
    }

    @Override
    public List<Track> getAllConnectedTracks() {
        List<Track> tracks = new LinkedList<>(Arrays.asList(previousTrack, nextStraightTrack, nextDivergingTrack));
        tracks.removeIf(Objects::isNull);
        return tracks;
    }

    @Override
    public void removeConnectedTrack(Track track) {
        if (track.equals(previousTrack)) {
            previousTrack = null;
        } else if (track.equals(nextStraightTrack)) {
            nextStraightTrack = null;
        } else if (track.equals(nextDivergingTrack)) {
            nextDivergingTrack = null;
        } else {
            throw new IllegalArgumentException("The track is not connected to this track");
        }
    }

    @Override
    public Vector getDirection() {
        return currentDirection;
    }

    @Override
    public void setDirection(Vector currentPosition) throws TrackException {
        if (!currentPosition.equals(straightEnd) && !currentPosition.equals(divergingEnd)) {
            throw new TrackException("The new direction must be one of the end points");
        }
        this.currentPosition = currentPosition;
        this.currentDirection = currentPosition.subtract(start).normalize();
    }

    @Override
    public Vector getNextPointInDirection(Vector point, Vector direction) {
        if (point.equals(start) && direction.equals(straightEnd.subtract(start).normalize())) {
            return straightEnd;
        } else if (point.equals(start) && direction.equals(divergingEnd.subtract(start).normalize())) {
            return divergingEnd;
        } else if (point.equals(straightEnd) && direction.equals(start.subtract(straightEnd).normalize())) {
            return start;
        } else if (point.equals(divergingEnd) && direction.equals(start.subtract(divergingEnd).normalize())) {
            return start;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX).append(StringConstants.SPACE);
        builder.append(super.getId());
        builder.append(StringConstants.SPACE);
        builder.append(start);
        builder.append(StringConstants.SPACE).append(StringConstants.ARROW).append(StringConstants.SPACE);
        builder.append(straightEnd);
        builder.append(StringConstants.COMMA);
        builder.append(divergingEnd);

        if (isCurrentPositionSet()) {
            try {
                builder.append(StringConstants.SPACE).append(getLength());
            } catch (TrackException e) {
                // This shouldn't happen because of the above check
                throw new IllegalStateException(e);
            }
        }
        return builder.toString();
    }
}
