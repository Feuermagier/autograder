package edu.kit.informatik.model.track;

import edu.kit.informatik.model.IdSupplier;
import edu.kit.informatik.model.Vector;

import java.util.List;

/**
 * Represents an abstract track with an unambiguous id. Each track can have any number of end points,
 * but while trains are on the track, there must be exactly two active end point between which a connection is created.
 *
 * @author uxxxx
 * @version 1.0
 */
public abstract class Track {
    private static final IdSupplier ID_SUPPLIER = new IdSupplier();

    private final int id;

    /**
     * Creates a new track and generates a individual id
     */
    public Track() {
        this.id = ID_SUPPLIER.nextId();
    }

    /**
     * Returns the id of this track
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Removes the id of this track from the global track id pool
     */
    public void remove() {
        ID_SUPPLIER.removeId(this.id);
    }

    /**
     * Returns the length of this track
     *
     * @return The length of this track
     * @throws TrackException If the length cannot be determined
     */
    public abstract int getLength() throws TrackException;

    /**
     * Gets the opposite end of this track which is currently active
     *
     * @param point The end for which the opposite should be found
     * @return The opposite end
     * @throws TrackException If the other end cannot be determined
     */
    public abstract Vector getOtherActiveEnd(Vector point) throws TrackException;

    /**
     * Gets the first active end. While trains are on the track, each track must have exactly two active ends
     * between which a connection is made by the track
     *
     * @return The first active end
     * @throws TrackException If the first active end cannot be determined
     */
    public abstract Vector getFirstActiveEnd() throws TrackException;

    /**
     * Gets the second active end. While trains are on the track, each track must have exactly two active ends
     * between which a connection is made by the track
     *
     * @return The second active end
     * @throws TrackException If the second active end cannot be determined
     */
    public abstract Vector getSecondActiveEnd() throws TrackException;

    /**
     * Returns all end points of this track
     *
     * @return All end points of this track
     */
    public abstract List<Vector> getAllPoints();

    /**
     * Finds all tracks that are connected to this track
     *
     * @return All connected tracks
     */
    public abstract List<Track> getAllConnectedTracks();

    /**
     * Finds the track that is connected to this track at the specified point. The point must be one of the end points.
     *
     * @param point The point to which the track is connected
     * @return The track or null if there is no track connected
     */
    public abstract Track findConnectedTrack(Vector point);

    /**
     * Sets the connected track at the specified end point. This will overwrite any previously connected track.
     * The specified point must be one of the end points. The track may be null.
     *
     * @param point The end point at which the track should be set
     * @param track The track to connect
     */
    public abstract void setConnectedTrack(Vector point, Track track);

    /**
     * Removes the pointer to the specified track fro this track. The track must be connected to this track.
     *
     * @param track The track that should be removed
     */
    public abstract void removeConnectedTrack(Track track);

    /**
     * Checks if the given point is on this track
     *
     * @param point The point to check for
     * @return True if the point lies on the track, false otherwise
     * @throws TrackException If it cannot be determined if the point is on the track
     */
    public boolean isPointOnTrack(Vector point) throws TrackException {
        // We check if the point is on the same line as the track and then we check if it lies between the
        // active end points
        if (getDirection().equals(Vector.UP) || getDirection().equals(Vector.DOWN)) {
            return point.getX() == getFirstActiveEnd().getX()
                && getFirstActiveEnd().subtract(point).length() <= getLength()
                && getSecondActiveEnd().subtract(point).length() <= getLength();
        } else if (getDirection().equals(Vector.RIGHT) || getDirection().equals(Vector.LEFT)) {
            return point.getY() == getFirstActiveEnd().getY()
                && getFirstActiveEnd().subtract(point).length() <= getLength()
                && getSecondActiveEnd().subtract(point).length() <= getLength();
        } else {
            return false;
        }
    }

    /**
     * Gets the direction of this track
     *
     * @return The direction of this track
     */
    public abstract Vector getDirection();

    /**
     * Sets the direction of this track
     *
     * @param direction The new direction
     * @throws TrackException If the direction cannot be set
     */
    public abstract void setDirection(Vector direction) throws TrackException;

    public abstract Vector getNextPointInDirection(Vector point, Vector direction);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!this.getClass().isInstance(o)) return false;
        Track track = (Track) o;
        return id == track.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
