package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.property.BooleanProperty;
import yi.component.shared.component.YiRadioMenuItem;
import yi.component.shared.i18n.TextResource;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorAccelerator;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A stateful action item that exports to either a {@link javafx.scene.control.RadioButton}
 * as component, or {@link YiRadioMenuItem} as menu item. This action can be assigned a
 * {@link ToggleGroup} to enforce selection constraints.
 *
 * @see EditorBasicAction
 */
public class EditorRadioAction extends EditorAbstractAction<YiRadioMenuItem, Node> {

    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    public EditorRadioAction(@NotNull TextResource name,
                             @Nullable Consumer<EditorActionContext> action) {
        super(name, action);

        selectedProperty.addListener(newValue ->
            getCachedMenuItem().ifPresent(item -> item.setSelected(newValue))
        );
    }

    @Override
    protected @NotNull YiRadioMenuItem getAsMenuItemImpl() {
        var item = new YiRadioMenuItem(getText(), getIcon());
        item.setSelected(isSelected());
        return item;
    }

    @Override
    protected Node getAsComponentImpl() {
        return null; // TODO: Create a radio button
    }

    /**
     * Sets a toggle group for this radio action. Only one radio action in a toggle group
     * can be selected at any given time.
     * <p/>
     * The radio action does not belong to any toggle group by default.
     *
     * @param group Toggle group this action belongs to, never null.
     */
    public void setMenuToggleGroup(@NotNull ToggleGroup group) {
        Objects.requireNonNull(group, "ToggleGroup cannot be null.");

        YiRadioMenuItem menuItem = getAsMenuItem();
        group.getToggles().add(menuItem);
        group.selectedToggleProperty().addListener(toggle -> {
            var selectedToggle = group.getSelectedToggle();
            if (selectedToggle != null) {
                setSelected(group.getSelectedToggle().equals(menuItem));
            }
        });

        if (isSelected()) {
            menuItem.setSelected(true);
        }
    }

    /**
     * Sets the selected property for this action, which will also update the selected
     * state on exported components. If this action belongs in a toggle group, other
     * radio components will become de-selected and fire a de-selection event.
     *
     * @param isSelected true if the action is selected.
     */
    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * @return true if this action is currently selected.
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorRadioAction setAccelerator(EditorAccelerator acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public EditorRadioAction setInMenuBar(@NotNull EditorMainMenuType menu, double position) {
        super.setInMenuBar(menu, position);
        return this;
    }

    @Override
    public EditorRadioAction setText(@NotNull TextResource i18nKey) {
        super.setText(i18nKey);
        return this;
    }

    @Override
    public EditorRadioAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorRadioAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorRadioAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
