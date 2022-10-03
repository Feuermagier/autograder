package edu.kit.informatik.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages id's. Provides id's incrementally beginning at 1 and provides functionality of removing ids and later
 * reassigning them
 *
 * @author uxxxx
 * @version 1.0
 */
public final class IdSupplier {
    private final Set<Integer> usedIds = new HashSet<>();

    /**
     * Generates the next usable id, starting at 1 and reusing removed id's
     *
     * @return The next usable id
     */
    public int nextId() {
        int id = 1;
        while (usedIds.contains(id)) {
            id++;
        }
        usedIds.add(id);
        return id;
    }

    /**
     * Removes the specified id from the pool of used id's
     *
     * @param id The id that should be removed
     * @throws IllegalArgumentException If the specified id hasn't been used already
     */
    public void removeId(int id) throws IllegalArgumentException {
        if (!usedIds.remove(id)) {
            throw new IllegalArgumentException("The id doesn't exist");
        }
    }
}
