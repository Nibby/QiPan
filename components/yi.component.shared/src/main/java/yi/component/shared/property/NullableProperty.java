package yi.component.shared.property;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A wrapper for any object data type that is capable of notifying listeners for
 * value changes. This class is not intended for primitive data types for performance
 * reasons. Use the specialised primitive properties for those.
 * <p/>
 * Value for this property may be {@code null}. To enforce non-null constraint on values,
 * use {@link Property} instead.
 *
 * @param <T> Type of value represented by this property.
 *
 * @see Property Non-null Property
 */
public class NullableProperty<T> {

    private final Set<NullablePropertyListener<T>> listeners = new HashSet<>(2);
    private @Nullable T value;

    /**
     * Instantiates the variable with a default value of {@code null}.
     */
    public NullableProperty() {
        this(null);
    }

    /**
     * Instantiates the variable with a specified value.
     *
     * @param initialValue Initial value.
     */
    public NullableProperty(@Nullable T initialValue) {
        this.value = initialValue;
    }

    /**
     * Subscribes to value changes from this variable. New events will be generated
     * for each {@link #set(T)} call.
     *
     * @param l Listener to respond to new value changes.
     */
    public void addListener(NullablePropertyListener<T> l) {
        listeners.add(l);
    }

    /**
     * Removes an existing listener if it exists.
     *
     * @param l An existing listener to remove.
     */
    public void removeListener(NullablePropertyListener<T> l) {
        listeners.remove(l);
    }

    /**
     * Updates the value represented by this property and fires a
     * {@link PropertyListener#onValueChange(T)}} event.
     *
     * @param newValue New value for this variable.
     */
    public void set(T newValue) {
        this.value = newValue;

        listeners.forEach(l -> l.onValueChange(newValue));
    }

    /**
     *
     * @return Current value of the variable.
     */
    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

}
