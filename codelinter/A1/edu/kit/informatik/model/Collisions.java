package edu.kit.informatik.model;

import edu.kit.informatik.model.train.Train;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a set of collisions between trains. Automatically merges collisions in which the same trains are involved.
 *
 * @author ukidf
 * @version 1.0
 */
public class Collisions {
    private List<Set<Train>> collisions = new ArrayList<>();

    /**
     * Adds a new collision between the specified trains. It is possible to specify only one train to indicate that it
     * has overrun the track
     *
     * @param trains The trains that crashed
     */
    public void addCollision(Train... trains) {
        if (trains.length == 0) {
            throw new IllegalArgumentException("You must specify at least one train");
        }

        boolean collisionFound = false;
        for (Set<Train> collision : collisions) {
            if (Arrays.stream(trains).anyMatch(collision::contains)) {
                collisionFound = true;
                collision.addAll(Arrays.asList(trains));
                break;
            }
        }
        if (!collisionFound) {
            collisions.add(new HashSet<>(Arrays.asList(trains)));
        }
    }

    /**
     * Adds all collisions between trains from the specified Collisions object to this object
     *
     * @param collisions Tne collisions that should be added
     */
    public void addCollisions(Collisions collisions) {
        this.collisions.addAll(collisions.collisions);
    }

    /**
     * Returns the number of collisions. Counting collisions between same trains only ones
     *
     * @return The number of collisions
     */
    public int getCollisionCount() {
        return collisions.size();
    }

    /**
     * Executes the given Consumer on each train that collided
     *
     * @param action The Consumer
     */
    public void forEachTrain(Consumer<? super Train> action) {
        collisions.forEach(collision -> collision.forEach(action));
    }

    /**
     * Executes the given Consumer on each collision represented by a Collection of trains that collided
     *
     * @param action The Consumer
     */
    public void forEachCollision(Consumer<? super Collection<Train>> action) {
        collisions.forEach(action);
    }
}
