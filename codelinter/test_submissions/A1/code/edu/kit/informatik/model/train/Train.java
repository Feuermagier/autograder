package edu.kit.informatik.model.train;

import edu.kit.informatik.StringConstants;
import edu.kit.informatik.model.IdSupplier;
import edu.kit.informatik.model.Vector;
import edu.kit.informatik.model.rollingstock.RollingStock;
import edu.kit.informatik.model.rollingstock.TrainException;
import edu.kit.informatik.model.track.Track;
import edu.kit.informatik.model.track.TrackException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a train containing one or multiple parts (rolling stocks).
 *
 * @author ukidf
 * @version 1.0
 */
public class Train {
    private static final IdSupplier ID_SUPPLIER = new IdSupplier();

    private final int id;
    private final List<RollingStock> parts = new LinkedList<>();

    /**
     * While a train is placed on the track, each point it requires is stored in this list.
     * If this list is null, the train isn't placed on the track.
     */
    private List<TrainPoint> trainPoints = null;

    /**
     * Creates a new train with no parts and an unambiguous id
     */
    public Train() {
        this.id = ID_SUPPLIER.nextId();
    }

    /**
     * Adds a new part to this train if the part can be added
     *
     * @param newPart The part that should be added
     * @throws TrainException If the part cannot be added to the train (because the train is standing on the
     *                        or the coupling doesn't match / the part has already been added to a train).
     */
    public void addPart(RollingStock newPart) throws TrainException {
        if (isPlacedOnTrack()) {
            throw new TrainException("The train has been already put on the track");
        }

        if (newPart.getTrain() != null) {
            throw new TrainException("This rolling stock has already been added to a train");
        }

        if (parts.isEmpty()) {
            parts.add(newPart);
            newPart.setTrain(this);
        } else {
            RollingStock lastPart = parts.get(parts.size() - 1);
            if (lastPart.canBeFollowedBy(newPart) && newPart.getTrain() == null) {
                parts.add(newPart);
                newPart.setTrain(this);
            } else {
                throw new TrainException("The couplings doesn't match");
            }
        }
    }

    /**
     * Checks whether this train is valid (i.e. contains a powered rolling stock at the first / last position)
     *
     * @return True if the train is valid
     */
    public boolean isValid() {
        if (parts.isEmpty()) {
            return false;
        } else {
            return parts.get(0).isPowered()
                || parts.get(parts.size() - 1).isPowered();
        }
    }

    /**
     * Calculates all points of this train by the head point. Also checks if the train would collide with itself or
     * is to long to fit on the track.
     *
     * @param headPoint The head point of this track
     * @throws TrackException If a switch that is needed id not set
     * @throws TrainException If the train cannot be placed on the track
     */
    public void placeOnTrack(TrainPoint headPoint) throws TrackException, TrainException {
        trainPoints = new LinkedList<>();
        trainPoints.add(headPoint);
        for (int i = 0; i < getLength(); i++) {
            TrainPoint nextPoint = getNextPosition(trainPoints.get(trainPoints.size() - 1), true);
            if (nextPoint != null) {
                trainPoints.add(nextPoint);
            } else {
                trainPoints = null;
                throw new TrainException("The train doesn't fit on the track system");
            }
        }
        if (trainPoints.stream().map(TrainPoint::getPosition).distinct().count() != trainPoints.size()) {
            trainPoints = null;
            throw new TrainException("The train would collide with itself");
        }
    }

    /**
     * Gets the position of the head of this train
     *
     * @return The head's position
     * @throws IllegalStateException If the track hasn't been placed on the track
     */
    public Vector getHeadPosition() throws IllegalStateException {
        if (isPlacedOnTrack()) {
            return trainPoints.get(0).getPosition();
        } else {
            throw new IllegalStateException("The train isn't placed on the track");
        }
    }

    /**
     * Removes this train from the track
     */
    public void derail() {
        trainPoints = null;
    }

    /**
     * Checks whether this train has been placed on the track
     *
     * @return True if this train has been placed on the track, false otherwise
     */
    public boolean isPlacedOnTrack() {
        return trainPoints != null;
    }

    /**
     * Returns all points of this train
     *
     * @return All points of this train
     */
    public List<TrainPoint> getTrainPoints() {
        if (!isPlacedOnTrack()) {
            throw new IllegalStateException("This track hasn't been placed on the track");
        }
        return trainPoints;
    }

    /**
     * Return the total length of this train
     *
     * @return The length
     */
    public long getLength() {
        return parts.stream().mapToLong(RollingStock::getLength).sum();
    }

    /**
     * Gets the id
     *
     * @return value of id
     */
    public int getId() {
        return id;
    }

    /**
     * Removes the id of this train from the global train id pool
     * and releases all rolling stock associated with this train
     */
    public void remove() {
        ID_SUPPLIER.removeId(this.id);
        parts.forEach(p -> p.setTrain(null));
    }

    /**
     * Returns a visual representation of this train
     *
     * @return The visual representation
     */
    public String getVisualRepresentation() {
        StringBuilder builder = new StringBuilder();
        int maxHeight = parts.stream()
            .mapToInt(part -> part.getVisualRepresentation().getHeight()).max().orElse(0);

        for (int i = maxHeight - 1; i >= 0; i--) {
            for (RollingStock part : parts) {
                builder.append(part.getVisualRepresentation().getLine(i)).append(StringConstants.SPACE);
            }
            builder.setLength(builder.length() - 1);
            if (i != 0) {
                builder.append(StringConstants.NEW_LINE);
            }
        }
        return builder.toString();
    }

    /**
     * Moves this train one step forward along the track. The train has to be placed on the track before calling this.
     *
     * @return True if the movement was possible, false if the train has overrun the track
     * @throws TrackException If a required switch has not been set
     */
    public boolean moveForward() throws TrackException {
        if (!isPlacedOnTrack() || trainPoints.size() != getLength() + 1) {
            throw new IllegalStateException("The train hasn't been placed on the track");
        }

        trainPoints.remove(trainPoints.size() - 1);
        TrainPoint nextPoint = getNextPosition(trainPoints.get(0), false);
        if (nextPoint == null) {
            return false;
        } else {
            trainPoints.add(0, nextPoint);
            return true;
        }
    }

    /**
     * Moves this train one step backwards along the track. The train has to be placed on the track before calling this.
     *
     * @return True if the movement was possible, false if the train has overrun the track
     * @throws TrackException If a required switch has not been set
     */
    public boolean moveBackwards() throws TrackException {
        if (!isPlacedOnTrack() || trainPoints.size() != getLength() + 1) {
            throw new IllegalStateException("The train hasn't been placed on the track");
        }

        trainPoints.remove(0);
        TrainPoint nextPoint = getNextPosition(trainPoints.get(trainPoints.size() - 1), true);
        if (nextPoint == null) {
            return false;
        } else {
            trainPoints.add(nextPoint);
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return id == train.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return id + StringConstants.SPACE
            + parts.stream().map(RollingStock::getId).collect(Collectors.joining(StringConstants.SPACE));
    }

    /**
     * Calculates the next point the given TrainPoint will reach when moving one step. Null indicates the TrainPoint
     * would overrun the track.
     *
     * @param point           The current point
     * @param invertDirection If the direction of the point should be inverted (i.e. move backwards)
     * @return The next TrainPoint or null
     * @throws TrackException If a required switch is not set
     */
    private TrainPoint getNextPosition(TrainPoint point, boolean invertDirection) throws TrackException {
        Vector direction = point.getDirection();

        if (invertDirection) {
            direction = direction.inverted();
        }

        Track nextTrack;
        Vector nextDirection;

        // Check if the next point in the current direction lies on the current track
        if (point.getTrack().isPointOnTrack(point.getPosition().add(direction))) {
            // If yes, we can keep the current direction and track
            nextTrack = point.getTrack();
            nextDirection = direction;
        } else {
            // If not, we have to determine the connected track (if any)
            nextTrack = point.getTrack().findConnectedTrack(point.getPosition());
            // The second condition is false if we approach a switch onto which we cannot move
            if (nextTrack == null || !nextTrack.isPointOnTrack(point.getPosition())) {
                return null;
            }
            nextDirection = nextTrack.getOtherActiveEnd(point.getPosition()).subtract(point.getPosition()).normalize();
        }

        // Calculate the next position
        Vector nextPoint = point.getPosition().add(nextDirection);

        // Re-invert the direction if we inverted it previously
        if (invertDirection) {
            nextDirection = nextDirection.inverted();
        }
        return new TrainPoint(nextPoint, nextDirection, nextTrack);
    }
}
