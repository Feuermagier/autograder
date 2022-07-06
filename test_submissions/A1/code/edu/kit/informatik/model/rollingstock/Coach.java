package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.StringConstants;
import edu.kit.informatik.model.IdSupplier;

import java.util.Objects;

/**
 * Represents a single coach of any type (passenger coach, freight coach or a special coach)
 *
 * @author ukidf
 * @version 1.0
 */
public class Coach extends RollingStock {

    /**
     * The prefix to indicate that an id belongs to a coach
     */
    public static final String PREFIX = "W";

    private static final String COACH = "coach";
    private static final String NONE = "none";
    private static final String PASSENGER_SHORT = "p";
    private static final String FREIGHT_SHORT = "f";
    private static final String SPECIAL_SHORT = "s";

    private static final IdSupplier ID_SUPPLIER = new IdSupplier();

    private final int id;
    private final Type type;

    /**
     * Creates a new coach
     *
     * @param length        The length of this rolling stock
     * @param frontCoupling Set this value to true, if the rolling stock should have a coupling at its front
     * @param backCoupling  Set this value to true, if the rolling stock should have a coupling at its back
     * @param type          The type of this coach
     * @throws TrainException If neither the front nor the back coupling is set to true
     */
    public Coach(int length, boolean frontCoupling, boolean backCoupling, Type type) throws TrainException {
        super(length, frontCoupling, backCoupling, false);
        this.type = type;

        id = ID_SUPPLIER.nextId();
    }

    @Override
    public String getId() {
        return PREFIX + id;
    }

    /**
     * Gets the integer id without a leading 'W'
     *
     * @return The id
     */
    public int getNumericalId() {
        return id;
    }

    @Override
    public String toString() {
        return id + StringConstants.SPACE
            + (getTrain() != null ? getTrain().getId() : NONE) + StringConstants.SPACE
            + type.getShortRepresentation() + StringConstants.SPACE
            + getLength() + StringConstants.SPACE
            + hasFrontCoupling() + StringConstants.SPACE
            + hasBackCoupling();
    }

    @Override
    public VisualRepresentation getVisualRepresentation() {
        return type.getGraphicalRepresentation();
    }

    @Override
    public String getTypeString() {
        return type.toString().toLowerCase() + StringConstants.SPACE + COACH;
    }

    @Override
    protected String getCouplingType() {
        return RollingStock.STANDARD_COUPLING;
    }

    /**
     * Removes the id of this coach from the global coach id pool
     */
    public void remove() {
        ID_SUPPLIER.removeId(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coach coach = (Coach) o;
        return id == coach.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * The available types of a coach
     */
    public enum Type {
        /**
         * A passenger coach
         */
        PASSENGER(PASSENGER_SHORT,
            "____________________",
            "|  ___ ___ ___ ___ |",
            "|  |_| |_| |_| |_| |",
            "|__________________|",
            "|__________________|",
            "   (O)        (O)   "),
        /**
         * A freight coach
         */
        FREIGHT(FREIGHT_SHORT,
            "|                  |",
            "|                  |",
            "|                  |",
            "|__________________|",
            "   (O)        (O)   "),
        /**
         * A special coach
         */
        SPECIAL(SPECIAL_SHORT,
            "               ____",
            "/--------------|  |",
            "\\--------------|  |",
            "  | |          |  |",
            " _|_|__________|  |",
            "|_________________|",
            "   (O)       (O)   ");

        private final String shortRepresentation;
        private final VisualRepresentation visualRepresentation;

        private Type(String shortRepresentation, String... visualRepresentation) {
            this.shortRepresentation = shortRepresentation;
            this.visualRepresentation = new VisualRepresentation(visualRepresentation);
        }

        /**
         * Gets the visual representation
         *
         * @return value of visualRepresentation
         */
        public VisualRepresentation getGraphicalRepresentation() {
            return visualRepresentation;
        }

        /**
         * Gets the short string representation
         *
         * @return value of shortRepresentation
         */
        public String getShortRepresentation() {
            return shortRepresentation;
        }
    }
}
