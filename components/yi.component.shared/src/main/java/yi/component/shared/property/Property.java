package yi.component.shared.property;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper for any object data type that is capable of notifying listeners for
 * value changes. This class is not intended for primitive data types for performance
 * reasons. Use the specialised primitive properties for those.
 * <p/>
 * Values in this property must not be null. To allow nullable values, use
 * {@link NullableProperty} instead.
 *
 * @param <T> Type of value represented by this property.
 *
 * @see NullableProperty Nullable Property
 */
public class Property<T> {

    private final Set<PropertyListener<T>> listeners = new HashSet<>(2);
    private T value;

    /**
     * Instantiates the variable with a specified value.
     *
     * @param initialValue Initial value.
     */
    public Property(@NotNull T initialValue) {
        assertNonNull(initialValue);
        this.value = initialValue;
    }

    /**
     * Subscribes to value changes from this variable. New events will be generated
     * for each {@link #set(T)} call.
     *
     * @param l Listener to respond to new value changes.
     */
    public void addListener(PropertyListener<T> l) {
        listeners.add(l);
    }

    /**
     * Removes an existing listener if it exists.
     *
     * @param l An existing listener to remove.
     */
    public void removeListener(PropertyListener<T> l) {
        listeners.remove(l);
    }

    /**
     * Updates the value represented by this property and fires a
     * {@link PropertyListener#onValueChange(T)}} event.
     *
     * @param newValue New value for this variable.
     */
    public void set(@NotNull T newValue) {
        assertNonNull(newValue);

        this.value = newValue;

        listeners.forEach(l -> l.onValueChange(newValue));
    }

    /**
     *
     * @return Current value of the variable.
     */
    public T get() {
        return value;
    }

    private void assertNonNull(T value) {
        Objects.requireNonNull(value, "Value must not be null");
    }
}
