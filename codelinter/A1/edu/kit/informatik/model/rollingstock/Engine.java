package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.StringConstants;

/**
 * Represents a single engine of any type (electrical engine, steam engine or diesel engine)
 *
 * @author ukidf
 * @version 1.0
 */
public class Engine extends PoweredRollingStock {

    private static final String ENGINE = "engine";
    private static final String NONE = "none";
    private static final String ELECTRICAL_SHORT = "e";
    private static final String STEAM_SHORT = "s";
    private static final String DIESEL_SHORT = "d";

    private final Type type;

    /**
     * Creates a new engine
     *
     * @param length        The length of this rolling stock
     * @param frontCoupling Set this value to true, if the rolling stock should have a coupling at its front
     * @param backCoupling  Set this value to true, if the rolling stock should have a coupling at its back
     * @param name          The name
     * @param typeSeries    The class / type series
     * @param type          The type of this engine
     * @throws TrainException If neither the front nor the back coupling is set to true or the type series is
     *                        set to 'W'
     */
    public Engine(int length, boolean frontCoupling, boolean backCoupling, String name, String typeSeries, Type type)
        throws TrainException {

        super(length, frontCoupling, backCoupling, name, typeSeries);
        this.type = type;
    }

    @Override
    public VisualRepresentation getVisualRepresentation() {
        return type.getVisualRepresentation();
    }


    @Override
    public String getTypeString() {
        return type.toString().toLowerCase() + StringConstants.SPACE + ENGINE;
    }

    @Override
    protected String getCouplingType() {
        return RollingStock.STANDARD_COUPLING;
    }

    @Override
    public String toString() {
        return (getTrain() != null ? getTrain().getId() : NONE) + StringConstants.SPACE
            + type.getShortRepresentation() + StringConstants.SPACE
            + getTypeSeries() + StringConstants.SPACE
            + getName() + StringConstants.SPACE
            + getLength() + StringConstants.SPACE
            + hasFrontCoupling() + StringConstants.SPACE
            + hasBackCoupling();
    }

    /**
     * The available types of engines
     */
    public enum Type {
        /**
         * An electrical engine
         */
        ELECTRICAL(ELECTRICAL_SHORT,
            "               ___    ",
            "                 \\    ",
            "  _______________/__  ",
            " /_| ____________ |_\\ ",
            "/   |____________|   \\",
            "\\                    /",
            " \\__________________/ ",
            "  (O)(O)      (O)(O)  "),
        /**
         * A steam engine
         */
        STEAM(STEAM_SHORT,
            "     ++      +------",
            "     ||      |+-+ | ",
            "   /---------|| | | ",
            "  + ========  +-+ | ",
            " _|--/~\\------/~\\-+ ",
            "//// \\_/      \\_/   "),
        /**
         * A diesel engine
         */
        DIESEL(DIESEL_SHORT,
            "  _____________|____  ",
            " /_| ____________ |_\\ ",
            "/   |____________|   \\",
            "\\                    /",
            " \\__________________/ ",
            "  (O)(O)      (O)(O)  ");

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
        public VisualRepresentation getVisualRepresentation() {
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
