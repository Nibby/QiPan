package yi.component.shared.property;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for primitive {@code boolean} data type that is capable of notifying
 * listeners of value changes.
 */
public class BooleanProperty {

    private final Set<BooleanPropertyListener> listeners = new HashSet<>(2);
    private boolean value;

    /**
     * Instantiates the variable with a default value of {@code false}.
     */
    public BooleanProperty() {
        this(false);
    }

    /**
     * Instantiates the variable with a specified value.
     *
     * @param initialValue Initial value.
     */
    public BooleanProperty(boolean initialValue) {
        this.value = initialValue;
    }

    /**
     * Subscribes to value changes from this variable. New events will be generated
     * for each {@link #set(boolean)} call.
     *
     * @param l Listener to respond to new value changes.
     */
    public void addListener(BooleanPropertyListener l) {
        listeners.add(l);
    }

    /**
     * Removes an existing listener if it exists.
     *
     * @param l An existing listener to remove.
     */
    public void removeListener(@NotNull BooleanPropertyListener l) {
        listeners.remove(l);
    }

    /**
     * Updates the value represented by this property and fires a
     * {@link BooleanPropertyListener#onValueChange(boolean)} event.
     *
     * @param newValue New value for this variable.
     */
    public void set(boolean newValue) {
        this.value = newValue;

        listeners.forEach(l -> l.onValueChange(newValue));
    }

    /**
     *
     * @return Current value of the variable.
     */
    public boolean get() {
        return value;
    }

}
