package edu.kit.informatik.repository;

import edu.kit.informatik.model.rollingstock.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Manages the rolling stocks that has been created. Provides methods to create, delete and search for rolling stock.
 *
 * @author ukidf
 * @version 1.0
 */
public class RollingStockRepository {
    private final List<Coach> coaches = new LinkedList<>();
    private final List<Engine> engines = new LinkedList<>();
    private final List<TrainSet> trainSets = new LinkedList<>();

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

        Engine engine = new Engine(length, couplingFront, couplingBack, name, typeSeries, type);
        if (engines.stream().anyMatch(e -> e.getId().equals(engine.getId()))
            || trainSets.stream().anyMatch(t -> t.getId().equals(engine.getId()))) {

            throw new TrainException("There is already a engine / train set with the specified id");
        } else {
            engines.add(engine);
            return engine;
        }
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

        Coach coach = new Coach(length, couplingFront, couplingBack, type);
        coaches.add(coach);
        return coach;
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

        TrainSet trainSet = new TrainSet(length, couplingFront, couplingBack, name, typeSeries);
        if (engines.stream().anyMatch(e -> e.getId().equals(trainSet.getId()))
            || trainSets.stream().anyMatch(t -> t.getId().equals(trainSet.getId()))) {

            throw new TrainException("There is already a engine / train set with the specified id");
        } else {
            trainSets.add(trainSet);
            return trainSet;
        }
    }

    /**
     * Gets all coaches
     *
     * @return value of coaches
     */
    public List<Coach> getCoaches() {
        return coaches;
    }

    /**
     * Gets all engines
     *
     * @return value of engines
     */
    public List<Engine> getEngines() {
        return engines;
    }

    /**
     * Gets all trainS sets
     *
     * @return value of trainSets
     */
    public List<TrainSet> getTrainSets() {
        return trainSets;
    }

    /**
     * Deletes the rolling stock with the specified id. For coaches, the id should start with 'W'
     *
     * @param id The id of the rolling stock with a leading 'W' for coaches
     * @throws TrainException If there is no rolling stock with this id
     */
    public void deleteRollingStock(String id) throws TrainException {
        Optional<Coach> coach = coaches.stream().filter(s -> s.getId().equals(id)).findAny();
        Optional<Engine> engine = engines.stream().filter(s -> s.getId().equals(id)).findAny();
        Optional<TrainSet> trainSet = trainSets.stream().filter(s -> s.getId().equals(id)).findAny();

        if (coach.isPresent()) {
            coach.get().remove();
            coaches.remove(coach.get());
        } else if (engine.isPresent()) {
            engines.remove(engine.get());
        } else if (trainSet.isPresent()) {
            trainSets.remove(trainSet.get());
        } else {
            throw new TrainException("There is no rolling stock with id " + id);
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
        Optional<RollingStock> rollingStock
            = Stream.concat(Stream.concat(engines.stream(), trainSets.stream()), coaches.stream())
            .filter(r -> r.getId().equals(id)).findAny();

        if (rollingStock.isPresent()) {
            return rollingStock.get();
        } else {
            throw new TrainException("There is no rolling stock with id " + id);
        }
    }
}
