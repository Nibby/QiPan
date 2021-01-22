package yi.component.shared.property;

/**
 * Listens to value changes from a {@link IntProperty}.
 */
@FunctionalInterface
public interface IntPropertyListener {

    /**
     * Informs the listener that the {@link IntProperty} value has changed.
     *
     * @param newValue New property value.
     */
    void onValueChange(int newValue);

}
