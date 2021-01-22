package yi.component.shared.property;

/**
 * Listens to value changes from a {@link BooleanProperty}.
 */
@FunctionalInterface
public interface BooleanPropertyListener {

    /**
     * Informs the listener that the {@link BooleanProperty} value has changed.
     *
     * @param newValue New property value.
     */
    void onValueChange(boolean newValue);

}
