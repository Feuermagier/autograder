package edu.kit.informatik.model.rollingstock;

import java.util.Objects;

/**
 * Represents a powered rolling stock (i.e. rolling stock that can drive by itself). A rolling stock can be
 * identified by it's type series (class) and it's name.
 *
 * @author uxxxx
 * @version 1.0
 */
public abstract class PoweredRollingStock extends RollingStock {
    private static final String ID_SEPARATOR = "-";

    private final String name;
    private final String typeSeries;

    /**
     * Creates a new powered rolling stock (i.e. rolling stock that can drive by itself)
     *
     * @param length        The length of this rolling stock
     * @param frontCoupling True if this rolling stock has a coupling a it's front
     * @param backCoupling  True if this rolling stock has a coupling a it's back
     * @param name          The name
     * @param typeSeries    The class / type series
     * @throws TrainException If neither the front nor the back coupling is set to true or the type series is
     *                        set to 'W'
     */
    public PoweredRollingStock(int length, boolean frontCoupling, boolean backCoupling, String name, String typeSeries)
        throws TrainException {

        super(length, frontCoupling, backCoupling, true);
        if (typeSeries.equals(Coach.PREFIX)) {
            throw new TrainException("The class must not be '" + Coach.PREFIX + "'");
        }

        this.name = name;
        this.typeSeries = typeSeries;
    }

    /**
     * Gets the name
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type series (class)
     *
     * @return value of typeSeries
     */
    public String getTypeSeries() {
        return typeSeries;
    }


    @Override
    public String getId() {
        return typeSeries + ID_SEPARATOR + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeSeries);
    }
}
