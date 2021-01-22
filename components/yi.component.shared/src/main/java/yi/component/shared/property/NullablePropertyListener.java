package yi.component.shared.property;

import org.jetbrains.annotations.Nullable;

/**
 * Listens to value changes from a {@link NullableProperty}.
 *
 * @param <T> Property value type.
 */
public interface NullablePropertyListener<T> {

    /**
     * Informs the listener that the {@link NullableProperty} value has changed.
     *
     * @param newValue New property value, may be null.
     */
    void onValueChange(@Nullable T newValue);


}
