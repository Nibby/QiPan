package yi.editor.framework.action;

import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.boardviewer.GameBoardViewer;
import yi.component.boardviewer.editmodes.AbstractEditMode;
import yi.component.shared.property.BooleanProperty;
import yi.component.shared.property.NullableProperty;
import yi.component.shared.component.YiRadioMenuItem;
import yi.component.shared.component.YiToggleButton;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.IconUtilities;
import yi.editor.components.EditorMainMenuType;
import yi.editor.components.EditorTool;
import yi.editor.framework.EditorAccelerator;

import java.util.Optional;
import java.util.function.Predicate;

public class EditorToolAction extends EditorAbstractAction<YiRadioMenuItem, YiToggleButton> {

    private final NullableProperty<ToggleGroup> componentGroup = new NullableProperty<>(null);
    private final NullableProperty<ToggleGroup> menuItemGroup = new NullableProperty<>(null);

    private final BooleanProperty selectedProperty = new BooleanProperty(false);

    private EditorToolAction(@NotNull TextResource name) {
        super(name, null);

        selectedProperty.addListener(this::updateSelectionState);

        componentGroup.addListener(newValue ->
                getCachedComponent().ifPresent(comp -> comp.setToggleGroup(newValue))
        );

        menuItemGroup.addListener(newValue ->
                getCachedMenuItem().ifPresent(menuItem -> menuItem.setToggleGroup(newValue))
        );
    }

    private void updateSelectionState(boolean isSelectedNow) {
        getCachedMenuItem().ifPresent(item -> {
            if (isSelectedNow != item.isSelected()) {
                item.setSelected(isSelectedNow);
            }
        });
        getCachedComponent().ifPresent(component -> {
            if (isSelectedNow != component.isSelected()) {
                component.setSelected(isSelectedNow);
            }
        });
    }

    @Override
    protected @NotNull YiRadioMenuItem getAsMenuItemImpl() {
        var item = new YiRadioMenuItem(getText(), getIcon());
        item.selectedProperty().addListener(event -> setSelected(item.isSelected()));
        getMenuItemToggleGroup().ifPresent(item::setToggleGroup);
        item.setSelected(isSelected());
        return item;
    }

    @Override
    protected @Nullable YiToggleButton getAsComponentImpl() {
        var toggleButton = new YiToggleButton(getText(), getIcon());
        toggleButton.selectedProperty().addListener(event -> setSelected(toggleButton.isSelected()));
        getComponentToggleGroup().ifPresent(toggleButton::setToggleGroup);
        toggleButton.setSelected(isSelected());
        return toggleButton;
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

    public static EditorToolAction forTool(final EditorTool tool,
                                           final ToggleGroup componentGroup,
                                           final ToggleGroup menuGroup,
                                           TextResource label,
                                           @Nullable String iconPath,
                                           @Nullable EditorAccelerator acceleratorId,
                                           Predicate<AbstractEditMode> selectionCriteria,
                                           double menuPosition) {

        var action = new EditorToolAction(label) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var boardViewer = getGameBoardViewer(context);
                var mode = boardViewer.getEditMode();
                var selected = selectionCriteria.test(mode);
                setSelected(selected);
            }
        };
        action.setAction(context -> {
            var window = context.getEditorWindow();
            var board = window.getBoardArea().getGameBoardViewer();

            if (action.isSelected()) {
                tool.apply(getGameBoardViewer(context));
                window.getGameModel().getEditor().setEditable(true);
            } else if (componentGroup.getSelectedToggle() == null && board.isEditable()) {
                window.getGameModel().getEditor().setEditable(false);
            }
        });
        action.setInMenuBar(EditorMainMenuType.TOOLS, menuPosition);
        if (acceleratorId != null) {
            action.setAccelerator(acceleratorId);
        }

        action.setShowIconOnMenuItem(false);
        action.setShowIconOnComponent(true);

        YiToggleButton actionComponent = action.getAsComponent();
        assert actionComponent != null : "Editor tool action component must not be null";

        if (iconPath != null) {
            String iconNormal = "/yi/editor/icons/" + iconPath + "_white32.png";
            String iconSelected = iconNormal.replace("_white32.png", "32.png");
            IconUtilities.loadIcon(iconNormal, EditorTool.class, 16).ifPresent(action::setIcon);
            actionComponent.selectedProperty().addListener(event -> {
                boolean selectedNow = actionComponent.isSelected();
                String icon = selectedNow ? iconSelected : iconNormal;
                IconUtilities.loadIcon(icon, EditorTool.class, 16).ifPresent(action::setIcon);
            });
        }
        action.setComponentToggleGroup(componentGroup);
        action.setMenuToggleGroup(menuGroup);

        return action;
    }

    private static GameBoardViewer getGameBoardViewer(EditorActionContext context) {
        var window = context.getEditorWindow();
        var board  = window.getBoardArea();
        return board.getGameBoardViewer();
    }
}
