package yi.component.shared.property;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for primitive {@code int} data type that is capable of notifying
 * listeners of value changes.
 */
public class IntProperty {

    private final Set<IntPropertyListener> listeners = new HashSet<>(2);
    private int value;

    /**
     * Instantiates the variable with a default value of {@code 0}.
     */
    public IntProperty() {
        this(0);
    }

    /**
     * Instantiates the variable with a specified value.
     *
     * @param initialValue Initial value.
     */
    public IntProperty(int initialValue) {
        this.value = initialValue;
    }

    /**
     * Subscribes to value changes from this variable. New events will be generated
     * for each {@link #set(int)} call.
     *
     * @param l Listener to respond to new value changes.
     */
    public void addListener(IntPropertyListener l) {
        listeners.add(l);
    }

    /**
     * Removes an existing listener if it exists.
     *
     * @param l An existing listener to remove.
     */
    public void removeListener(IntPropertyListener l) {
        listeners.remove(l);
    }

    /**
     * Updates the value represented by this property and fires a
     * {@link BooleanPropertyListener#onValueChange(boolean)} event.
     *
     * @param newValue New value for this variable.
     */
    public void set(int newValue) {
        this.value = newValue;

        listeners.forEach(l -> l.onValueChange(newValue));
    }

    /**
     *
     * @return Current value of the variable.
     */
    public int getValue() {
        return value;
    }

}
