package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.editor.EditorMainMenuType;
import yi.editor.framework.accelerator.EditorAcceleratorId;

/**
 * Defines one actionable task in the game editor. Each action must be atomic and
 * self-contained, in that it does not depend on another action to run.
 * <p/>
 * Two types of actions exist:
 * <ol>
 *     <li>
 *         <b>Shared Actions</b>: actions that are universally common across all editor
 *         dialogs. These actions are typically stateless and do not depend on instance-
 *         specific state to operate. Actions under this category include "New Game",
 *         "Open", "Save" etc.
 *     </li>
 *     <li>
 *         <b>Instance-specific Actions</b>: actions that operate based on the current
 *         state of the editor frame. These actions are updated on a frame-by-frame basis.
 *         An example is the perspective settings for each editor window.
 *     </li>
 * </ol>
 * Actions can be set to show in the menu bar through {@link #setInMainMenu(EditorMainMenuType, double)}.
 * Optionally, an action can be exported to a JavaFx {@link Node} via {@link #getAsComponent()}.
 *
 * @see EditorActionManager
 */
public interface EditorAction {

    /**
     * @return Locale-specific text for the component(s) representing this action.
     */
    @NotNull String getLocalisedName();

    /**
     * @return {@link TextResource} used for localisation.
     */
    @NotNull TextResource getName();

    /**
     * Sets the {@link TextResource} used for localisation. All text resources in
     * the editor module are found in {@link yi.editor.EditorTextResources}.
     *
     * @param name Text resource for localisation.
     * @return this instance for method chaining.
     */
    EditorAction setName(@NotNull TextResource name);

    /**
     * Sets the graphic used on the exported action component. The menu component
     * should have the icon set always, but depending on implementation,
     * {@link #getAsComponent()} might not always return a component with the icon.
     *
     * @param icon Desired icon to show on the exported component.
     * @return this instance for method chaining.
     */
    EditorAction setIcon(@Nullable ImageView icon);

    /**
     * @return Desired graphic to show on the exported component.
     */
    @Nullable ImageView getIcon();

    /**
     * Sets the shortcut key combination for the {@link #getAsMenuItem() menu component}.
     * <p/>
     * A list of all mapped accelerators can be found in {@link EditorAcceleratorId}.
     *
     * @param acceleratorId Accelerator ID to be applied to the menu item component.
     * @return this instance for method chaining.
     */
    EditorAction setAccelerator(EditorAcceleratorId acceleratorId);

    /**
     * Sets whether this action should be added to the main application menu. If
     * this method is not called, then the action will not be added by default.
     *
     * @param menu Main menu category to appear under. It is possible to add this
     *             action to a sub-menu within the main category. In that case the
     *             menu type should still be the same top-level menu the sub-menu
     *             belongs to.
     * @param position Position of this menu item, a double value usually between
     *                 0 to 1.0, where 0 indicates the absolute top of the menu.
     *                 See {@link #getMenuPosition()}.
     * @return this instance for method chaining.
     */
    EditorAction setInMainMenu(@NotNull EditorMainMenuType menu, double position);

    /**
     * @return {@code true} if this action should be added to the main menu.
     */
    boolean isInMainMenu();

    /**
     * TODO: I think this is more about implementation detail and should not be part
     *       of the general contract...
     *
     * @return {@code true} if this component {@link #isInMainMenu() is in main menu} and
     *         it does not belong in a sub-menu within that menu.
     */
    boolean isTopLevelMenuItem();

    /**
     * @return Position value indicating where the exported menu item sits relative to the
     * other menu items in the same popup. The value is usually between 0.0 - 1.0d,
     * where 0.0 indicates the top of the popup.
     */
    double getMenuPosition();

    /**
     * This value is only relevant if the action {@link #isInMainMenu() is in main menu}.
     *
     * @return Main menu category in which this action will export its menu item under.
     * @see #setInMainMenu(EditorMainMenuType, double)
     */
    @NotNull EditorMainMenuType getMainMenuType();

    /**
     * Exports this action as a {@link MenuItem}.
     *
     * @return Main menu item component for this action.
     */
    @NotNull MenuItem getAsMenuItem();

    /**
     * Exports this action as a JavaFx {@link Node} component. It is not guaranteed that
     * all actions support this feature, so the result may be null.
     *
     * @return {@link Node} component for this action, may be null.
     */
    @Nullable Node getAsComponent();

    /**
     * Sets the enabled-ness of this action. The state will be updated immediately
     * on all existing exported components.
     *
     * @param enabled true if enabled.
     * @return this instance for method chaining.
     */
    EditorAction setEnabled(boolean enabled);

    /**
     * @return Enabled-ness status of this action.
     */
    boolean isEnabled();

    /**
     * Sets the visibility of the exported components for this action.
     *
     * @param visible true if visible.
     * @return this instance for method chaining.
     */
    EditorAction setVisible(boolean visible);

    /**
     * @return Visibility status for exported components.
     */
    boolean isVisible();

    /**
     * Marks this action as having been added to the main menu already, so that
     * it can be ignored if it is to be added again.
     * <p/>
     * @apiNote This is more of an implementation detail for {@link EditorSubMenuAction}.
     */
    void markAsAddedToMenu();

    /**
     * @return true if the menu item component for this action has been added to main menu
     * already.
     */
    boolean isAddedToMenu();

    /**
     * Executes the action represented by this model.
     *
     * @param helper Helper class supplying the context in which this action is executed
     *               in, such as the {@link yi.editor.EditorFrame} that invoked it.
     */
    void performAction(EditorActionContext helper);
}