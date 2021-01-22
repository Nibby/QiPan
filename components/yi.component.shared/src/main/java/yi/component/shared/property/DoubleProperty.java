package yi.component.shared.property;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for primitive {@code double} data type that is capable of notifying
 * listeners of value changes.
 */
public class DoubleProperty {

    private final Set<DoublePropertyListener> listeners = new HashSet<>(2);
    private double value;

    /**
     * Instantiates the variable with a default value of {@code 0d}.
     */
    public DoubleProperty() {
        this(0d);
    }

    /**
     * Instantiates the variable with a specified value.
     *
     * @param initialValue Initial value.
     */
    public DoubleProperty(double initialValue) {
        this.value = initialValue;
    }

    /**
     * Subscribes to value changes from this variable. New events will be generated
     * for each {@link #set(double)} call.
     *
     * @param l Listener to respond to new value changes.
     */
    public void addListener(DoublePropertyListener l) {
        listeners.add(l);
    }

    /**
     * Removes an existing listener if it exists.
     *
     * @param l An existing listener to remove.
     */
    public void removeListener(DoublePropertyListener l) {
        listeners.remove(l);
    }

    /**
     * Updates the value represented by this property and fires a
     * {@link DoublePropertyListener#onValueChange(double)} event.
     *
     * @param newValue New value for this variable.
     */
    public void set(double newValue) {
        this.value = newValue;

        listeners.forEach(l -> l.onValueChange(newValue));
    }

    /**
     *
     * @return Current value of the variable.
     */
    public double get() {
        return value;
    }

}
