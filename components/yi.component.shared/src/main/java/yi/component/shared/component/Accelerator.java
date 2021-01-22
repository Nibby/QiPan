package yi.component.shared.component;

import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.YiWindow;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.GuiUtilities;

import java.util.*;

/**
 * Represents a {@link KeyCombination} for a particular feature or action.
 * <p/>
 * To set the accelerator for a supported Fx component, use {@link #install(MenuItem)}
 * or one of its overloaded variants.
 */
public final class Accelerator {

    private static final Map<String, Accelerator> ALL_ACCELERATORS = new HashMap<>();

    private final String id;
    private final TextResource name;
    private final KeyModifier[] modifiers;
    private final KeyCode keyCode;
    private KeyCombination keyCombination = null;

    public Accelerator(String id, TextResource name, KeyCode keyCode, KeyModifier[] modifiers) {
        if (ALL_ACCELERATORS.containsKey(id)) {
            throw new IllegalStateException("Duplicated shortcut key AcceleratorId: " + getId());
        }

        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.modifiers = Objects.requireNonNull(modifiers, "Modifiers must not be null");
        this.keyCode = Objects.requireNonNull(keyCode, "Key code must not be null");
        register(this);
    }

    /**
     * @return Internal unique identifier for the accelerator.
     */
    public String getId() {
        return id;
    }

    /**
     * @return User-friendly description of the purpose of this accelerator.
     */
    public String getName() {
        return name.getLocalisedText();
    }

    /**
     * @return The Fx key combination represented by this accelerator.
     */
    public KeyCombination getKeyCombination() {
        if (keyCombination != null) {
            return keyCombination;
        }

        keyCombination = GuiUtilities.getKeyCombination(keyCode, modifiers);
        return keyCombination;
    }

    /**
     * Installs the shortcut key represented by this accelerator to the menu item.
     *
     * @param menuItem Item to have the shortcut key applied.
     */
    public void install(@NotNull MenuItem menuItem) {
        menuItem.acceleratorProperty().setValue(getKeyCombination());
    }

    /**
     * Adds a global shortcut key to the scene. This means the action will be executed
     * as long as the key combination is active, regardless of the currently focused
     * component within the scene.
     *
     * @param window Window to install shortcut key.
     * @param action Action to perform once the key combination is active.
     */
    public void install(@NotNull YiWindow window, Runnable action) {
        window.getScene().installAccelerator(getKeyCombination(), action);
    }

    void setKeyCombination(@NotNull KeyCombination newCombination) {
        this.keyCombination = Objects.requireNonNull(newCombination);
    }

    private static void register(@NotNull Accelerator accelerator) {
        Objects.requireNonNull(accelerator);
        ALL_ACCELERATORS.put(accelerator.getId(), accelerator);
    }

    public static Map<String, Accelerator> getAllAccelerators() {
        return Collections.unmodifiableMap(ALL_ACCELERATORS);
    }

    public static void setAcceleratorKeyCombination(String acceleratorId,
                                                    KeyCombination keyCombination) {
        var accelerator = getAccelerator(acceleratorId);
        accelerator.ifPresent(it -> it.setKeyCombination(keyCombination));
    }

    public static Optional<Accelerator> getAccelerator(String id) {
        return Optional.ofNullable(ALL_ACCELERATORS.get(id));
    }

    /**
     * Sets the menu item with a shortcut key defined by an accelerator mapped to the
     * given id. This will overwrite any existing shortcut keys set on the menu item.
     *
     * @param id Unique ID of the accelerator to be installed.
     * @param menuItem Menu item component to have shortcut keys set.
     */
    public static void install(String id, MenuItem menuItem) {
        getAccelerator(id).ifPresentOrElse(it -> it.install(menuItem), () -> Accelerator.noAcceleratorWithId(id));
    }

    /**
     * Adds an additional shortcut key to trigger some action on the given scene.
     *
     * @param id Unique ID of the accelerator to be installed.
     * @param window Window to have shortcut keys added.
     * @param action Code to run when the shortcut key is active.
     */
    public static void install(String id, YiWindow window, Runnable action) {
        getAccelerator(id).ifPresentOrElse(it -> it.install(window, action), () -> Accelerator.noAcceleratorWithId(id));
    }

    private static void noAcceleratorWithId(String id) {
        throw new IllegalStateException("No accelerators registered with ID: " + id);
    }
}
