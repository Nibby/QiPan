package yi.component.shared.property;

import org.jetbrains.annotations.NotNull;

/**
 * Listens to value changes from a {@link Property}.
 *
 * @param <T> Property value type.
 */
@FunctionalInterface
public interface PropertyListener<T> {

    /**
     * Informs the listener that the {@link Property} value has changed.
     *
     * @param newValue New property value.
     */
    void onValueChange(@NotNull T newValue);

}
