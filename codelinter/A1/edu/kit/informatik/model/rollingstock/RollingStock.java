package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.model.train.Train;

/**
 * An abstract rolling stock with a length. This class also stores the train this rolling
 * stock belongs to (if there is any)
 *
 * @author ukidf
 * @version 1.0
 */
public abstract class RollingStock {

    /**
     * A default String to use for rolling stock that can be coupled with any other standard rolling stock
     */
    protected static final String STANDARD_COUPLING = "s";


    private final int length;
    private final boolean frontCoupling;
    private final boolean backCoupling;
    private final boolean powered;

    private Train train = null;

    /**
     * Creates a new rolling stock
     *
     * @param length        The length of this rolling stock
     * @param frontCoupling True if this rolling stock has a coupling a it's front
     * @param backCoupling  True if this rolling stock has a coupling a it's back
     * @param powered       If this rolling stock is powered
     * @throws TrainException If neither the front nor the back coupling is set to true or the length is smaller than 1
     */
    public RollingStock(int length, boolean frontCoupling, boolean backCoupling, boolean powered)
        throws TrainException {

        if (length < 1) {
            throw new TrainException("The length must be at least 1");
        }

        this.length = length;
        this.frontCoupling = frontCoupling;
        this.backCoupling = backCoupling;
        this.powered = powered;
    }

    /**
     * Checks whether it is possible to add the specified rolling stock behind this rolling stock
     *
     * @param rollingStock The rolling stock that will be aded
     * @return True if the rollingStock can be added
     */
    public boolean canBeFollowedBy(RollingStock rollingStock) {
        if (this.hasBackCoupling() && rollingStock.hasFrontCoupling()) {
            return this.getCouplingType().equals(rollingStock.getCouplingType());
        } else {
            return false;
        }
    }

    /**
     * Gets powered
     *
     * @return value of powered
     */
    public boolean isPowered() {
        return powered;
    }

    /**
     * Gets the length of this rolling stock
     *
     * @return The length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the train this rolling stock belongs to
     *
     * @return The train of this rolling stock or null if the rolling stock doesn't belong to any train
     */
    public Train getTrain() {
        return train;
    }

    /**
     * Sets the train of this rolling stock
     *
     * @param train The new value
     */
    public void setTrain(Train train) {
        this.train = train;
    }

    /**
     * Gets frontCoupling
     *
     * @return value of frontCoupling
     */
    protected boolean hasFrontCoupling() {
        return frontCoupling;
    }

    /**
     * Gets backCoupling
     *
     * @return value of backCoupling
     */
    protected boolean hasBackCoupling() {
        return backCoupling;
    }

    /**
     * Returns a visual representation of this rolling stock
     *
     * @return The visual representation
     */
    public abstract VisualRepresentation getVisualRepresentation();

    /**
     * Creates a type string for this rolling stock. This is not unique!
     *
     * @return A string representing the type of this rolling stock
     */
    public abstract String getTypeString();

    /**
     * Returns the id string of this rolling stock
     *
     * @return The id string
     */
    public abstract String getId();

    /**
     * Gets the type of the coupling of this rolling stock
     *
     * @return The coupling type
     */
    protected abstract String getCouplingType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PoweredRollingStock that = (PoweredRollingStock) o;
        return getId().equals(that.getId());
    }
}
