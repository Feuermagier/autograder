package edu.kit.informatik;

import edu.kit.informatik.cmd.CommandException;
import edu.kit.informatik.model.Collisions;
import edu.kit.informatik.model.Vector;
import edu.kit.informatik.model.rollingstock.Coach;
import edu.kit.informatik.model.rollingstock.Engine;
import edu.kit.informatik.model.rollingstock.RollingStock;
import edu.kit.informatik.model.rollingstock.TrainException;
import edu.kit.informatik.model.rollingstock.TrainSet;
import edu.kit.informatik.model.track.StandardTrack;
import edu.kit.informatik.model.track.Switch;
import edu.kit.informatik.model.track.Track;
import edu.kit.informatik.model.track.TrackException;
import edu.kit.informatik.model.train.Train;
import edu.kit.informatik.model.train.TrainPoint;
import edu.kit.informatik.repository.RollingStockRepository;
import edu.kit.informatik.repository.TrackRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The central class that manages the application state and provides all necessary methods to change it according
 * to the assignment. Maintains an instance of TrackRepository and RollingStockRepository along all created trains.
 *
 * @author ukidf
 * @version 1.0
 */
public final class Simulation {
    private final TrackRepository trackRepository = new TrackRepository();
    private final RollingStockRepository rollingStockRepository = new RollingStockRepository();

    private final List<Train> trains = new LinkedList<>();

    /**
     * Adds a new track between start and end and does all necessary checks (i.e. no overlapping tracks, ..)
     *
     * @param start The starting point of the new track
     * @param end   The end of the track
     * @return The newly created track
     * @throws TrackException If the track cannot be placed correctly
     */
    public StandardTrack addStandardTrack(Vector start, Vector end) throws TrackException {
        StandardTrack newTrack = trackRepository.addStandardTrack(start, end);
        trackRepository.recalculateTrackOccupation(trains);
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
        Switch newSwitch = trackRepository.addSwitch(start, straightEnd, divergingEnd);
        trackRepository.recalculateTrackOccupation(trains);
        return newSwitch;
    }

    /**
     * Removes the track withe the given id from the list of all tracks and releases it's id
     *
     * @param id The id of the track
     * @throws TrackException If there is no track with the give id or removing this track separates the track system
     */
    public void removeTrack(int id) throws TrackException {
        trackRepository.removeTrack(id);
    }

    /**
     * Gets all tracks
     *
     * @return All tracks
     */
    public List<Track> getTracks() {
        return trackRepository.getTracks();
    }

    /**
     * Sets the direction of the switch with the given id
     *
     * @param id        The id of the switch
     * @param direction The direction to which the switch should be set
     * @throws TrackException If there is no switch with the given id
     */
    public void setSwitch(int id, Vector direction) throws TrackException {
        trackRepository.setSwitch(id, direction);
        trackRepository.recalculateTrackOccupation(trains);
    }

    /**
     * Creates a new engine
     *
     * @param type          The engine's type
     * @param typeSeries    The engine's type series
     * @param name          The engine's name
     * @param length        The engine's length (>= 1)
     * @param couplingFront If the engine has a coupling at its front
     * @param couplingBack  If the engine has a coupling at its back
     * @return The newly created engine
     * @throws TrainException If the engine already exists or a parameter is invalid
     */
    public Engine createEngine(Engine.Type type, String typeSeries, String name, int length,
                               boolean couplingFront, boolean couplingBack) throws TrainException {

        return rollingStockRepository.createEngine(type, typeSeries, name, length, couplingFront, couplingBack);
    }

    /**
     * Creates a new coach
     *
     * @param type          The coach's type
     * @param length        The coach's length
     * @param couplingFront If the coach has a coupling at its front
     * @param couplingBack  If the coach has a coupling at its back
     * @return The newly created coach
     * @throws TrainException If the coach already exists or the parameter i invalid
     */
    public Coach createCoach(Coach.Type type, int length, boolean couplingFront, boolean couplingBack)
        throws TrainException {

        return rollingStockRepository.createCoach(type, length, couplingFront, couplingBack);
    }

    /**
     * Creates a new train set
     *
     * @param typeSeries    The train set's type series
     * @param name          The train set's name
     * @param length        The train set's length
     * @param couplingFront If the train set has a coupling at it s front
     * @param couplingBack  If the train set has a coupling at its back
     * @return The newly created train set
     * @throws TrainException If the train set already exists or any parameter is wrong
     */
    public TrainSet createTrainSet(String typeSeries, String name, int length,
                                   boolean couplingFront, boolean couplingBack) throws TrainException {

        return rollingStockRepository.createTrainSet(typeSeries, name, length, couplingFront, couplingBack);
    }

    /**
     * Gets all coaches
     *
     * @return value of coaches
     */
    public List<Coach> getCoaches() {
        return rollingStockRepository.getCoaches();
    }

    /**
     * Gets all engines
     *
     * @return value of engines
     */
    public List<Engine> getEngines() {
        return rollingStockRepository.getEngines();
    }

    /**
     * Gets all trainS sets
     *
     * @return value of trainSets
     */
    public List<TrainSet> getTrainSets() {
        return rollingStockRepository.getTrainSets();
    }

    /**
     * Gets all trains
     *
     * @return value of trains
     */
    public List<Train> getTrains() {
        return trains;
    }

    /**
     * Deletes the rolling stock with the specified id. For coaches, the id should start with 'W'
     *
     * @param id The id of the rolling stock with a leading 'W' for coaches
     * @throws TrainException If there is no rolling stock with this id
     */
    public void deleteRollingStock(String id) throws TrainException {
        rollingStockRepository.deleteRollingStock(id);
    }

    /**
     * Adds the specified rolling stock to the specified train or creates a new train if no train with this id exists.
     *
     * @param trainId        The id of the train
     * @param rollingStockId The id of the rolling stock with a leading 'W' for coaches
     * @return The train to which the rolling stock has been added
     * @throws TrainException If there is no train / rolling stock with this id
     */
    public Train addTrain(int trainId, String rollingStockId) throws TrainException {
        Optional<Train> train = trains.stream().filter(t -> t.getId() == trainId).findAny();
        if (train.isPresent()) {
            train.get().addPart(findRollingStockById(rollingStockId));
            return train.get();
        } else {
            Train newTrain = new Train();
            if (newTrain.getId() == trainId) {
                newTrain.addPart(findRollingStockById(rollingStockId));
                trains.add(newTrain);
                return newTrain;
            } else {
                newTrain.remove();
                throw new TrainException("The train id is not the smallest usable");
            }
        }
    }

    /**
     * Gets the rolling stock with the specified id
     *
     * @param id The id of the rolling stock
     * @return The rolling stock
     * @throws TrainException If there is no rolling stock with this id
     */
    public RollingStock findRollingStockById(String id) throws TrainException {
        return rollingStockRepository.findRollingStockById(id);
    }

    /**
     * Removes the rolling stock with the specified id
     *
     * @param id The id of the rolling stock
     * @throws TrainException If there is no rolling stock with this id
     */
    public void removeTrain(int id) throws TrainException {
        Train train = findTrain(id);
        train.derail();
        trains.remove(train);
        train.remove();

        // Remove all rolling stock from this train
        rollingStockRepository.getEngines().stream().filter(engine ->
            train.equals(engine.getTrain())).forEach(engine -> engine.setTrain(null));
        rollingStockRepository.getTrainSets().stream().filter(trainSet ->
            train.equals(trainSet.getTrain())).forEach(trainSet -> trainSet.setTrain(null));
        rollingStockRepository.getCoaches().stream().filter(coach ->
            train.equals(coach.getTrain())).forEach(coach -> coach.setTrain(null));

        trackRepository.recalculateTrackOccupation(trains);
    }

    /**
     * Puts a train on the track
     *
     * @param id         The train's id
     * @param position   The position of the train
     * @param xDirection The x direction of the train's head
     * @param yDirection The y direction of the train's head
     * @throws CommandException If there is no train with this id or the train / placing is invalid or not all
     *                          switches are set
     */
    public void putTrain(int id, Vector position, int xDirection, int yDirection) throws CommandException {
        if (trackRepository.isAnySwitchNotSet()) {
            throw new TrackException("Not all switches are set");
        }

        Train train = findTrain(id);
        if (train.isPlacedOnTrack()) {
            throw new TrainException("The train has been already placed on the track");
        }
        if (!train.isValid()) {
            throw new TrainException("The train is not valid");
        }

        Vector direction = new Vector(xDirection, yDirection).normalize();
        // Determine the track the train head is placed on
        Track headTrack = null;
        for (Track track : trackRepository.getTracks()) {
            // Check if not only the head of this train is on the track
            // According to the assignment sheet the give the direction has
            // to match the (inverted) direction of the track the train is placed on (i.e. the direction
            // cannot be parallel to a part of a switch that is not active)
            if (track.isPointOnTrack(position)
                && (track.getDirection().equals(direction) || track.getDirection().equals(direction.inverted()))) {

                headTrack = track;
                break;
            }
        }
        if (headTrack != null) {
            // Try to place the train on the track (will throw an exception if the train is to long to be placed)
            train.placeOnTrack(new TrainPoint(position, direction, headTrack));
            Set<Track> occupiedTracks = trackRepository.findOccupiedTracks(train);
            // Check if the train uses already occupied tracks
            occupiedTracks.retainAll(trackRepository.getTrackStates().keySet());
            if (!occupiedTracks.isEmpty()) {
                train.derail();
                throw new TrainException("The train would collide with another train");
            }
        } else {
            throw new TrainException("The train isn't placed on the track");
        }

        trackRepository.recalculateTrackOccupation(trains);
    }

    /**
     * Does n simulation steps with collision checking after each step
     *
     * @param steps The number of steps. Negative values indicate that the trains should drive backwards
     * @return All collisions that happened during the simulation
     * @throws TrackException If not all switches are set
     */
    public Collisions doSteps(int steps) throws TrackException {
        if (trackRepository.isAnySwitchNotSet()) {
            throw new TrackException("Not all switches are set");
        }

        Collisions allCollisions = new Collisions();
        for (int i = 0; i < Math.abs(steps); i++) {
            Collisions collisions = trackRepository.moveTrains(steps < 0, trains);
            collisions.addCollisions(trackRepository.calculateStepCollisions(trains));
            collisions.forEachTrain(Train::derail);
            allCollisions.addCollisions(collisions);
        }

        trackRepository.recalculateTrackOccupation(trains);
        return allCollisions;
    }

    /**
     * Gets a visual representation of this train as a String.
     *
     * @param id The train's id
     * @return The visual representation
     * @throws TrainException If there is no train with this id
     */
    public String showTrain(int id) throws TrainException {
        return findTrain(id).getVisualRepresentation();
    }


    private Train findTrain(int id) throws TrainException {
        Optional<Train> train = trains.stream().filter(t -> t.getId() == id).findAny();
        if (train.isPresent()) {
            return train.get();
        } else {
            throw new TrainException("There is no train with this id");
        }
    }
}
