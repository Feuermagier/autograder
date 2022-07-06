package edu.kit.informatik.repository;

import edu.kit.informatik.model.Collisions;
import edu.kit.informatik.model.Vector;
import edu.kit.informatik.model.track.StandardTrack;
import edu.kit.informatik.model.track.Switch;
import edu.kit.informatik.model.track.Track;
import edu.kit.informatik.model.track.TrackException;
import edu.kit.informatik.model.train.Train;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages the tracks (i.e. normal tracks and switches) that has been created. Provides functionality to check for
 * free tracks (for example while placing trains on tracks), to move trains (and check for collisions)
 * and to check that all tracks are
 * always connected.
 *
 * @author ukidf
 * @version 2.0
 */
public class TrackRepository {
    private final List<Track> tracks = new LinkedList<>();
    private final Map<Track, Train> trackStates = new HashMap<>();

    /**
     * Adds a new track between start and end and does all necessary checks (i.e. no overlapping tracks, ..)
     *
     * @param start The starting point of the new track
     * @param end   The end of the track
     * @return The newly created track
     * @throws TrackException If the track cannot be placed correctly
     */
    public StandardTrack addStandardTrack(Vector start, Vector end) throws TrackException {
        if (tracks.isEmpty()) {
            StandardTrack newTrack = new StandardTrack(start, end);
            tracks.add(newTrack);
            return newTrack;
        }
        if (!isConnectionPossible(start) && !isConnectionPossible(end)) {
            throw new TrackException("The track is not connected to any other track");
        }
        StandardTrack newTrack = new StandardTrack(start, end);

        findTrackWithEndPoint(start).ifPresent(track -> connectTracks(newTrack, track, start));
        findTrackWithEndPoint(end).ifPresent(track -> connectTracks(newTrack, track, end));

        tracks.add(newTrack);
        return newTrack;
    }

    /**
     * Adds a new switch between start, straightEnd and divergingEnd
     * and does all necessary checks (i.e. no overlapping tracks, ..)
     *
     * @param start        The starting point of the new switch
     * @param straightEnd  The straight end point of the new switch
     * @param divergingEnd The diverging end point of the new switch
     * @return The newly created switch
     * @throws TrackException If the switch cannot be placed correctly
     */
    public Switch addSwitch(Vector start, Vector straightEnd, Vector divergingEnd) throws TrackException {
        if (tracks.isEmpty()) {
            Switch newSwitch = new Switch(start, straightEnd, divergingEnd);
            tracks.add(newSwitch);
            return newSwitch;
        }

        if (!isConnectionPossible(start) && !isConnectionPossible(straightEnd) && !isConnectionPossible(divergingEnd)) {
            throw new TrackException("The switch is not connected to any other track");
        }
        Switch newSwitch = new Switch(start, straightEnd, divergingEnd);

        findTrackWithEndPoint(start).ifPresent(track -> connectTracks(newSwitch, track, start));
        findTrackWithEndPoint(straightEnd).ifPresent(track -> connectTracks(newSwitch, track, straightEnd));
        findTrackWithEndPoint(divergingEnd).ifPresent(track -> connectTracks(newSwitch, track, divergingEnd));

        tracks.add(newSwitch);
        return newSwitch;
    }

    /**
     * Removes the track withe the given id from the list of all tracks and releases it's id
     *
     * @param id The id of the track
     * @throws TrackException If there is no track with the give id or removing this track separates the track system
     */
    public void removeTrack(int id) throws TrackException {
        Optional<Track> optionalTrack = tracks.stream().filter(t -> t.getId() == id).findAny();
        if (optionalTrack.isPresent()) {
            Track track = optionalTrack.get();
            if (trackStates.containsKey(track)) {
                throw new TrackException("There is a train on the track");
            }
            // Check if the track system would be split by removing this track
            if (!track.getAllConnectedTracks().isEmpty()) {
                // Try to reach all tracks except this track from any track that is connected to this track
                Track anyConnectedTrack = track.getAllConnectedTracks().get(0);
                Set<Track> foundTracks = trackDepthSearch(anyConnectedTrack, track, track);
                foundTracks.add(track);
                if (!(tracks.containsAll(foundTracks) && foundTracks.containsAll(tracks))) {
                    throw new TrackException("The system would be split into multiple parts");
                }
            }
            for (Track connectedTrack : track.getAllConnectedTracks()) {
                connectedTrack.removeConnectedTrack(track);
            }
            track.remove();
            tracks.remove(track);
        } else {
            throw new TrackException("There is no track with this id");
        }
    }

    /**
     * Gets all tracks
     *
     * @return All tracks
     */
    public List<Track> getTracks() {
        return tracks;
    }

    /**
     * Sets the direction of the switch with the given id
     *
     * @param id        The id of the switch
     * @param direction The direction to which the switch should be set
     * @throws TrackException If there is no switch with the given id
     */
    public void setSwitch(int id, Vector direction) throws TrackException {
        Optional<Track> track = tracks.stream().filter(t -> t.getId() == id).findAny();
        if (track.isPresent()) {
            track.get().setDirection(direction);
            if (trackStates.containsKey(track.get())) {
                // There was a train on this switch
                trackStates.get(track.get()).derail();
            }
        } else {
            throw new TrackException("There is no switch with this id");
        }
    }

    /**
     * Checks if all switches are set
     *
     * @return True if any switch is not set, false if all switches are set
     */
    public boolean isAnySwitchNotSet() {
        for (Track track : tracks) {
            if (track.getDirection() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves all trains exactly one step along the track and checks if any train overruns the track
     *
     * @param backwards True if the trains should move backwards, false otherwise
     * @param trains    All trains that are in the train system
     *                  (trains that are not placed on the track will not be moved)
     * @return A Collisions object containing all trains that have overrun the track
     * @throws TrackException If a switch is not set but has to be used
     */
    public Collisions moveTrains(boolean backwards, Collection<Train> trains) throws TrackException {
        Collisions collisions = new Collisions();
        for (Train train : trains) {
            if (!train.isPlacedOnTrack()) {
                continue;
            }
            boolean success;
            if (backwards) {
                success = train.moveBackwards();
            } else {
                success = train.moveForward();
            }
            if (!success) {
                collisions.addCollision(train);
            }
        }
        return collisions;
    }

    /**
     * Checks for train collisions by the rules of the step command (i.e. point collisions and trains only use tracks
     * at which a part is placed).
     *
     * @param trains All trains that should be checked. Trains that are not placed on the track system will be ignored
     * @return A Collisions object containing all collisions that have been detected
     */
    public Collisions calculateStepCollisions(Collection<Train> trains) {
        Collisions collisions = new Collisions();

        Map<Vector, Train> trackEndStates = new HashMap<>();
        Map<Track, Train> trackStates = new HashMap<>();

        for (Train train : trains) {
            if (!train.isPlacedOnTrack()) {
                continue;
            }

            for (int i = 0; i < train.getTrainPoints().size(); i++) {
                if (i == 0 || i == train.getTrainPoints().size() - 1) {
                    Train previousTrain = trackEndStates.put(train.getTrainPoints().get(i).getPosition(), train);
                    if (previousTrain != null && !previousTrain.equals(train)) {
                        collisions.addCollision(train, previousTrain);
                    }
                } else {
                    for (Track track : train.getTrainPoints().get(i).getOccupiedTracks()) {
                        Train previousTrain = trackStates.put(track, train);
                        if (previousTrain != null && !previousTrain.equals(train)) {
                            collisions.addCollision(train, previousTrain);
                        }
                    }
                }
            }
        }
        return collisions;
    }

    /**
     * Re-calculates which tracks are occupied by which trains. For occupation detection the same rules as at
     * findOccupiedTracks are applied.
     *
     * @param trains All trains including those not placed on the track (they will be ignored)
     */
    public void recalculateTrackOccupation(Collection<Train> trains) {
        trackStates.clear();
        for (Train train : trains) {
            if (!train.isPlacedOnTrack()) {
                continue;
            }
            findOccupiedTracks(train).forEach(track -> trackStates.put(track, train));
        }
    }

    /**
     * Returns all tracks that will be occupied by this train. The train must have been placed on the track.
     * The train occupies a track if at least one point of the train matches with at least one point of the track
     *
     * @param train The train for that the check should be run
     * @return All tracks the given train occupies
     */
    public Set<Track> findOccupiedTracks(Train train) {
        if (!train.isPlacedOnTrack()) {
            throw new IllegalArgumentException("The train has to be placed on the track");
        }
        return train.getTrainPoints().stream()
            .flatMap(point -> point.getOccupiedTracks().stream())
            .collect(Collectors.toSet());
    }

    /**
     * Returns all tracks that are occupied along with the trains that occupy them as of the last call to
     * recalculateTrackOccupation. Caution: This will !not! update the track occupation.
     *
     * @return Each occupied track mapped to the train that occupies it.
     */
    public Map<Track, Train> getTrackStates() {
        return trackStates;
    }

    /**
     * Implements a depth search through the track system. This method will be called recursive.
     *
     * @param track         The current track
     * @param previousTrack The previous track (we don't want to go back to this)
     * @param startingTrack The track at which the search has been started (should be skipped)
     * @return All tracks that have been found
     */
    private Set<Track> trackDepthSearch(Track track, Track previousTrack, Track startingTrack) {
        if (track.equals(startingTrack)) {
            return new HashSet<>();
        } else {
            Set<Track> foundTracks = new HashSet<>();
            foundTracks.add(track);
            for (Track nextTrack : track.getAllConnectedTracks()) {
                if (!nextTrack.equals(previousTrack)) {
                    foundTracks.addAll(trackDepthSearch(nextTrack, track, startingTrack));
                }
            }
            return foundTracks;
        }
    }

    private void connectTracks(Track firstTrack, Track secondTrack, Vector point) {
        firstTrack.setConnectedTrack(point, secondTrack);
        secondTrack.setConnectedTrack(point, firstTrack);
    }

    private Optional<Track> findTrackWithEndPoint(Vector point) {
        return tracks.stream().filter(t -> t.getAllPoints().contains(point)).findAny();
    }

    private boolean isConnectionPossible(Vector point) throws TrackException {
        Optional<Track> track = findTrackWithEndPoint(point);
        if (track.isPresent()) {
            if (track.get().findConnectedTrack(point) == null) {
                return true;
            } else {
                throw new TrackException("There is already a track connected at " + point);
            }
        } else {
            return false;
        }
    }
}
