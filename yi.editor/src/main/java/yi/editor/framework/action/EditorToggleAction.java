package yi.editor.framework.action;

import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.property.BooleanProperty;
import yi.component.shared.property.NullableProperty;
import yi.component.shared.component.YiCheckMenuItem;
import yi.component.shared.component.YiToggleButton;
import yi.component.shared.i18n.TextResource;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorAccelerator;

import java.util.Optional;
import java.util.function.Consumer;

public class EditorToggleAction extends EditorAbstractAction<YiCheckMenuItem, YiToggleButton> {

    private final NullableProperty<ToggleGroup> componentGroup = new NullableProperty<>(null);
    private final NullableProperty<ToggleGroup> menuItemGroup = new NullableProperty<>(null);
    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    public EditorToggleAction(@NotNull TextResource name,
                              @Nullable Consumer<EditorActionContext> action) {
        super(name, action);

        selectedProperty.addListener(newValue ->
            getCachedMenuItem().ifPresent(item -> item.setSelected(newValue))
        );

        componentGroup.addListener(newValue ->
            getCachedComponent().ifPresent(comp -> comp.setToggleGroup(newValue))
        );

        menuItemGroup.addListener(newValue ->
            getCachedMenuItem().ifPresent(menuItem -> menuItem.setToggleGroup(newValue))
        );
    }

    @Override
    protected @NotNull YiCheckMenuItem getAsMenuItemImpl() {
        var item = new YiCheckMenuItem(getText(), getIcon());
        item.setSelected(isSelected());
        return item;
    }

    @Override
    protected @Nullable YiToggleButton getAsComponentImpl() {
        var toggleButton = new YiToggleButton(getText(), getIcon());
        toggleButton.setSelected(isSelected());
        toggleButton.selectedProperty().addListener(actionEvent -> setSelected(toggleButton.isSelected()));
        return toggleButton;
    }

    /**
     * Sets the selected state of this toggle action. This will update
     * the status of exported components as well.
     *
     * @param isSelected true if this action is selected.
     */
    public void setSelected(boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * @return true if the action is currently selected.
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    /**
     * Set a toggle group for the component of this action created using
     * {@link #getAsComponent()}. Once set, only one item in the group may be
     * selected at any time. The parameter may be null, in which case the component will
     * not be set to any group.
     * <p/>
     * Note that this method only applies to the component, not the menu item. Use
     * {@link #setMenuToggleGroup(ToggleGroup)} to assign toggle groups to menu items.
     *
     * @param toggleGroup Component toggle group.
     */
    public void setComponentToggleGroup(@Nullable ToggleGroup toggleGroup) {
        this.componentGroup.set(toggleGroup);
    }

    /**
     * @return Toggle group the component for this action currently belongs to.
     * @see #setComponentToggleGroup(ToggleGroup)
     */
    public Optional<ToggleGroup> getComponentToggleGroup() {
        return componentGroup.get();
    }

    /**
     * Set a toggle group for the menu item of this action created using
     * {@link #getAsMenuItem()}. Once set, only one item in the group may be
     * selected at any time. The parameter may be null, in which case the menu item will
     * not be set to any group.
     * <p/>
     * Note that this method only applies to the menu item, not the component. Use
     * {@link #setComponentToggleGroup(ToggleGroup)} to assign toggle groups to components.
     *
     * @param toggleGroup Menu item toggle group.
     */
    public void setMenuToggleGroup(@Nullable ToggleGroup toggleGroup) {
        this.menuItemGroup.set(toggleGroup);
    }

    /**
     * @return Toggle group the menu item for this action currently belongs to.
     * @see #setMenuToggleGroup(ToggleGroup)
     */
    public Optional<ToggleGroup> getMenuItemToggleGroup() {
        return menuItemGroup.get();
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorToggleAction setAccelerator(EditorAccelerator acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public EditorToggleAction setInMenuBar(@NotNull EditorMainMenuType menu, double position) {
        super.setInMenuBar(menu, position);
        return this;
    }

    @Override
    public EditorToggleAction setText(@NotNull TextResource i18nKey) {
        super.setText(i18nKey);
        return this;
    }

    @Override
    public EditorToggleAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorToggleAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorToggleAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
