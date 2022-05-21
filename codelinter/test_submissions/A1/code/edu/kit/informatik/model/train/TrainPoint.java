package edu.kit.informatik.model.train;

import edu.kit.informatik.model.Vector;
import edu.kit.informatik.model.track.Track;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a point of a train containing its position, the direction this part is currently moving in and a track
 * that contains the position.
 *
 * @author ukidf
 * @version 1.0
 */
public class TrainPoint {
    private final Vector position;
    private final Vector direction;
    private final Track track;

    /**
     * Creates a new TrainPoint
     *
     * @param position  The position of this train point
     * @param direction The direction this train point is moving
     * @param track     A rack that contains this train point
     */
    public TrainPoint(Vector position, Vector direction, Track track) {
        this.position = position;
        this.direction = direction.normalize();
        this.track = track;
    }

    /**
     * Gets the position of this train point
     *
     * @return The position
     */
    public Vector getPosition() {
        return position;
    }

    /**
     * Gets the direction this train is moving in as a normalized vector
     *
     * @return The direction of this train point
     */
    public Vector getDirection() {
        return direction;
    }

    /**
     * Gets a track that contains the position. This track is unambiguous if the position is not an end point of a
     * track.
     *
     * @return The track of this train point
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Returns all tracks that train point is placed on. This is only one track if the position is not an end point
     * of a track, otherwise it may be two tracks,
     *
     * @return All tracks that train occupies as an unmodifiable list.
     */
    public List<Track> getOccupiedTracks() {
        if (track.getAllPoints().contains(position)) {
            return Stream.of(track, track.findConnectedTrack(position)).filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        } else {
            return Collections.singletonList(track);
        }
    }
}
