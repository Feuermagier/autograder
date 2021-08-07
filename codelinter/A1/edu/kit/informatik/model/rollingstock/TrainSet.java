package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.StringConstants;

/**
 * Represents a single train set that can be coupled only with other train sets of the same type
 *
 * @author flose
 * @version 1.0
 */
public class TrainSet extends PoweredRollingStock {

    private static final String TRAIN_SET = "train-set";
    private static final String NONE = "none";

    /**
     * Creates a new train set
     *
     * @param length        The length of this rolling stock
     * @param frontCoupling Set this value to true, if the rolling stock should have a coupling at its front
     * @param backCoupling  Set this value to true, if the rolling stock should have a coupling at its back
     * @param name          The name
     * @param typeSeries    The class / type series
     * @throws TrainException If neither the front nor the back coupling is set to true or the type series is
     *                        set to 'W'
     */
    public TrainSet(int length, boolean frontCoupling, boolean backCoupling, String name, String typeSeries)
        throws TrainException {

        super(length, frontCoupling, backCoupling, name, typeSeries);
    }

    @Override
    public VisualRepresentation getVisualRepresentation() {
        return new VisualRepresentation(
            "         ++         ",
            "         ||         ",
            "_________||_________",
            "|  ___ ___ ___ ___ |",
            "|  |_| |_| |_| |_| |",
            "|__________________|",
            "|__________________|",
            "   (O)        (O)   ");
    }

    @Override
    public String getTypeString() {
        return TRAIN_SET;
    }

    @Override
    protected String getCouplingType() {
        return getTypeSeries();
    }

    @Override
    public String toString() {
        return (getTrain() != null ? getTrain().getId() : NONE) + StringConstants.SPACE
                + getTypeSeries() + StringConstants.SPACE
                + getName() + StringConstants.SPACE
                + getLength() + StringConstants.SPACE
                + hasFrontCoupling() + StringConstants.SPACE
                + hasBackCoupling() + StringConstants.SPACE;
    }
}
