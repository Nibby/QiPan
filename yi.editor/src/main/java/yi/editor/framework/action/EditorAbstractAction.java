package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.property.BooleanProperty;
import yi.component.shared.property.NullableProperty;
import yi.component.shared.property.Property;
import yi.component.shared.component.Accelerator;
import yi.component.shared.i18n.TextResource;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.EditorHelper;
import yi.editor.framework.EditorAccelerator;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * This class is the base implementation for all actions in the editor module.
 */
public abstract class EditorAbstractAction<M extends MenuItem, C extends Node> implements EditorAction {

    private final Property<TextResource> nameProperty = new Property<>(EditorTextResources.EMPTY);
    private final NullableProperty<ImageView> iconProperty = new NullableProperty<>(null);
    private final BooleanProperty enabledProperty = new BooleanProperty(true);
    private final BooleanProperty visibleProperty = new BooleanProperty(true);
    private final BooleanProperty componentCompactProperty = new BooleanProperty(false);
    private final BooleanProperty showIconOnMenuItemProperty = new BooleanProperty(true);
    private final BooleanProperty showIconOnComponentProperty = new BooleanProperty(true);
    private EditorAction parentAction = null;

    private EditorActionContext context;
    private Runnable action;

    private boolean addedToMenu = false;
    private EditorMainMenuType mainMenuType = null;
    private double menuPosition = 0d;

    private final NullableProperty<M> createdMenuItem = new NullableProperty<>(null);
    private final NullableProperty<C> createdComponent = new NullableProperty<>(null);
    private final NullableProperty<EditorAccelerator> acceleratorId = new NullableProperty<>(null);

    private Object userObject = null;

    /**
     * Instantiates an action that does nothing when selected. Assign an action using
     * {@link #setAction(Consumer)}.
     *
     * @param name I18n translation key for this action.
     */
    public EditorAbstractAction(@NotNull TextResource name) {
        this(name, null);
    }

    /**
     * Instantiates an action.
     *
     * @param name I18n translation key for this action.
     * @param action Task to perform when this action is executed. May be null, which
     *               does nothing on interaction.
     */
    public EditorAbstractAction(@NotNull TextResource name,
                                @Nullable Consumer<EditorActionContext> action) {

        this.nameProperty.set(name);
        setAction(action);

        // TODO: Some listeners only work on cached components. Maybe initialize those
        //       lazily to save memory.
        addAcceleratorUpdateListener();
        addNameUpdateListener();
        addIconUpdateListener();
        addVisibilityListener();
        addEnabledStateListener();
        addCompactnessListener();
    }

    private void addCompactnessListener() {
        componentCompactProperty.addListener(isCompactNow -> {
            getCachedComponent().ifPresent(component -> {
                if (component instanceof Labeled) {
                    Labeled labeled = (Labeled) component;
                    updateNodeCompactness(labeled, isCompactNow);
                }
            });
            onComponentCompactnessUpdate(isCompactNow);
        });
    }

    private void updateNodeCompactness(Labeled node, boolean isCompact) {
        if (isCompact) {
            StringBuilder tipText = new StringBuilder(getLocalisedText());

            acceleratorId.get().ifPresent(id -> {
                var accelerator = EditorAccelerator.getAccelerator(id);
                var keyCombination = accelerator.getKeyCombination();
                tipText.append(" (").append(keyCombination.getDisplayText()).append(")");
            });
            node.setTooltip(new Tooltip(tipText.toString()));
            node.setText("");
        } else {
            node.setText(getLocalisedText());
            node.setTooltip(null);
        }
    }

    private void addEnabledStateListener() {
        enabledProperty.addListener(isEnabledNow -> {
            getCachedMenuItem().ifPresent(item -> item.setDisable(!isEnabledNow));
            getCachedComponent().ifPresent(node -> node.setDisable(!isEnabledNow));
            onEnabledStateUpdate(isEnabledNow);
        });
    }

    private void addVisibilityListener() {
        visibleProperty.addListener(isVisibleNow -> {
            getCachedMenuItem().ifPresent(item -> item.setVisible(isVisibleNow));
            getCachedComponent().ifPresent(node -> {
                node.setVisible(isVisibleNow);
                node.setManaged(isVisibleNow);
            });
            onVisibilityUpdate(isVisibleNow);
        });
    }

    private void addIconUpdateListener() {
        iconProperty.addListener(newIcon -> {
            if (isShowingIconOnMenuItem()) {
                createdMenuItem.get().ifPresent(item -> item.setGraphic(newIcon));
            }

            if (isShowingIconOnComponent()) {
                createdComponent.get().ifPresent(node -> {
                    if (node instanceof Labeled) {
                        ((Labeled) node).setGraphic(newIcon);
                    }
                });
            }
            onIconUpdate(newIcon);
        });

        showIconOnMenuItemProperty.addListener(showOnMenu -> {
            ImageView icon = showOnMenu ? getIcon() : null;
            getCachedMenuItem().ifPresent(item -> item.setGraphic(icon));
        });

        showIconOnComponentProperty.addListener(showOnComponent -> {
            ImageView icon = showOnComponent ? getIcon() : null;
            getCachedComponent().ifPresent(node -> {
                if (node instanceof Labeled) {
                    ((Labeled) node).setGraphic(icon);
                }
            });
        });
    }

    private void addNameUpdateListener() {
        nameProperty.addListener(newName -> {
            createdMenuItem.get().ifPresent(item -> item.setText(newName.getLocalisedText()));
            createdComponent.get().ifPresent(node -> {
                if (node instanceof Labeled) {
                    ((Labeled) node).setText(newName.getLocalisedText());
                }
            });
            onNameUpdate(newName);
        });
    }

    private void addAcceleratorUpdateListener() {
        acceleratorId.addListener(newAcceleratorId -> createdMenuItem.get().ifPresent(menuItem -> installMenuItemAccelerator(newAcceleratorId, menuItem)));
        createdMenuItem.addListener(newMenuItem -> acceleratorId.get().ifPresent(id -> installMenuItemAccelerator(id, newMenuItem)));
    }

    private void installMenuItemAccelerator(EditorAccelerator editorAccelerator, MenuItem menuItem) {
        if (EditorHelper.isRunningAsTest() && (editorAccelerator == null || menuItem == null)) {
            // TODO: Supposedly UI tests aren't loading all the accelerators but
            //       I think it should. Work out what's causing this and try remove
            //       this exclusion if possible.
            return;
        }
        var id = editorAccelerator.getId();
        Accelerator.install(id, menuItem);
    }

    @Override
    public void setContext(EditorActionContext context) {
        this.context = context;
    }

    /**
     * @return The current editor window context this action is working with.
     */
    protected final @NotNull EditorActionContext getContext() {
        return Objects.requireNonNull(context, "Context not yet initialized");
    }

    @Override
    public void setAction(Consumer<EditorActionContext> action) {
        if (action != null) {
            this.action = () -> {
                var context = getContext();
                action.accept(context);
            };
        } else {
            this.action = null;
        }
    }

    @Override
    public @NotNull String getLocalisedText() {
        return nameProperty.get().getLocalisedText();
    }

    @Override
    public EditorAction setAccelerator(EditorAccelerator acceleratorId) {
        this.acceleratorId.set(acceleratorId);
        return this;
    }

    @Override
    public EditorAction setInMenuBar(@NotNull EditorMainMenuType menu, double position) {
        this.mainMenuType = menu;
        this.menuPosition = position;
        return this;
    }

    @Override
    public boolean isInMenuBar() {
        return mainMenuType != null;
    }

    @Override
    public double getMenuPosition() {
        return menuPosition;
    }

    @Override
    public @NotNull EditorMainMenuType getMainMenuType() {
        return Objects.requireNonNull(mainMenuType,
                "Action is not set to show in the main menu");
    }

    @Override
    public @NotNull M getAsMenuItem() {
        if (!isInMenuBar()) {
            throw new IllegalStateException("Cannot create menu item for action that is " +
                    "not set to appear in the main menu. Action: " + getLocalisedText());
        }
        Optional<M> menuItem = createdMenuItem.get();

        if (menuItem.isPresent()) {
            return menuItem.get();
        }

        M newMenuItem = getAsMenuItemImpl();
        newMenuItem.setUserData(this);
        newMenuItem.setVisible(isVisible());
        newMenuItem.setDisable(!isEnabled());
        if (isShowingIconOnMenuItem()) {
            newMenuItem.setGraphic(getIcon());
        } else {
            newMenuItem.setGraphic(null);
        }
        newMenuItem.setText(getLocalisedText());
        createdMenuItem.set(newMenuItem);
        return newMenuItem;
    }

    protected abstract @NotNull M getAsMenuItemImpl();

    @Override
    public C getAsComponent() {
        Optional<C> component = createdComponent.get();

        if (component.isPresent()) {
            return component.get();
        }

        C newComponent = getAsComponentImpl();
        if (newComponent != null) {
            newComponent.setUserData(this);
            newComponent.setVisible(isVisible());
            newComponent.setDisable(!isEnabled());
            if (newComponent instanceof Labeled) {
                var labeled = (Labeled) newComponent;
                if (isShowingIconOnComponent()) {
                    labeled.setGraphic(getIcon());
                }
                labeled.setText(getLocalisedText());
                updateNodeCompactness(labeled, isComponentCompact());
            }
            if (newComponent instanceof ButtonBase) {
                var buttonBase = (ButtonBase) newComponent;
                buttonBase.setOnAction(actionEvent -> performAction());
            }
        }
        createdComponent.set(newComponent);
        return newComponent;
    }

    protected abstract @Nullable C getAsComponentImpl();

    /**
     * Notifies that the icon for this action has changed.
     *
     * @param newIcon New icon for this action.
     *
     * @apiNote The default implementation automatically updates
     * the icon of components that are subclasses of {@link Labeled}.
     */
    protected void onIconUpdate(ImageView newIcon) {

    }

    /**
     * Notifies that the name for this action has changed.
     *
     * @param newName New name for this action.
     *
     * @apiNote The default implementation automatically updates
     * the text of components that are subclasses of {@link Labeled}.
     */
    protected void onNameUpdate(TextResource newName) {

    }

    /**
     * Notifies that the enabled state for this action has changed.
     *
     * @param isEnabledNow Current enabled status for this action.
     *
     * @apiNote The default implementation automatically calls
     * {@link Node#setDisable(boolean)} on the exported menu item
     * and component.
     */
    protected void onEnabledStateUpdate(boolean isEnabledNow) {

    }

    /**
     * Notifies that the visibility of components for this action has
     * changed.
     *
     * @param isVisibleNow Current visibility status for this action.
     *
     * @apiNote The default implementation automatically calls
     * {@link Node#setVisible(boolean)} and {@link Node#setManaged(boolean)}
     * for exported components, and {@link MenuItem#setVisible(boolean)}
     * for menu items.
     */
    protected void onVisibilityUpdate(boolean isVisibleNow) {

    }

    /**
     * Notifies that the exported component should be compact now.
     *
     * @param isCompactNow Current compactness status for this action.
     *
     * @apiNote The default implementation automatically handles compactness
     * for all exported components that are subclasses of {@link Labeled}.
     */
    protected void onComponentCompactnessUpdate(boolean isCompactNow) {

    }

    @Override
    public void setComponentCompact(boolean compact) {
        componentCompactProperty.set(compact);
    }

    @Override
    public boolean isComponentCompact() {
        return componentCompactProperty.get();
    }

    @Override
    public void performAction() {
        if (action != null) {
            action.run();
        }
    }

    @Override
    public @NotNull TextResource getText() {
        return nameProperty.get();
    }

    @Override
    public EditorAction setText(@NotNull TextResource i18nKey) {
        this.nameProperty.set(i18nKey);
        return this;
    }

    @Override
    public EditorAction setIcon(@Nullable ImageView icon) {
        var iconToUse = icon;
        final int REQUIRED_SIZE = 16;

        if (iconToUse != null) {
            var image = icon.getImage();
            if (image.getWidth() > REQUIRED_SIZE || image.getWidth() != image.getHeight()) {
                var wrapper = new ImageView(iconToUse.getImage());
                wrapper.setFitWidth(REQUIRED_SIZE);
                wrapper.setFitHeight(REQUIRED_SIZE);
                iconToUse = wrapper;
            }
        }

        this.iconProperty.set(iconToUse);
        return this;
    }

    @Override
    public void setShowIconOnMenuItem(boolean showIcon) {
        showIconOnMenuItemProperty.set(showIcon);
    }

    @Override
    public boolean isShowingIconOnMenuItem() {
        return showIconOnMenuItemProperty.get();
    }

    @Override
    public void setShowIconOnComponent(boolean showIcon) {
        showIconOnComponentProperty.set(showIcon);
    }

    @Override
    public boolean isShowingIconOnComponent() {
        return showIconOnComponentProperty.get();
    }

    @Override
    public @Nullable ImageView getIcon() {
        return iconProperty.get().orElse(null);
    }

    @Override
    public EditorAction setEnabled(boolean enabled) {
        this.enabledProperty.set(enabled);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabledProperty.get();
    }

    @Override
    public EditorAction setVisible(boolean visible) {
        this.visibleProperty.set(visible);
        return this;
    }

    @Override
    public boolean isVisible() {
        return visibleProperty.get();
    }

    @Override
    public void markAsAddedToMenu() {
        addedToMenu = true;
    }

    @Override
    public boolean isAddedToMenu() {
        return addedToMenu;
    }

    protected final void setParentAction(EditorSubMenuAction action) {
        this.parentAction = action;
    }

    @Override
    public boolean isTopLevelMenuItem() {
        return parentAction == null;
    }

    /**
     * This method only returns the cached result, and will not create a menu item.
     *
     * @return The currently cached menu item as a result for {@link #getAsMenuItem()}.
     * The returned result may be empty, which means either the action was not set to
     * appear in the main menu, or {@link #getAsMenuItem()} has not been called yet.
     */
    protected Optional<M> getCachedMenuItem() {
        return createdMenuItem.get();
    }

    /**
     * @return The currently cached component as a result of calling {@link #getAsComponent()}.
     * The returned result may be null, which means either the action does not support
     * exporting to component, or {@link #getAsComponent()} has not been called yet.
     */
    protected Optional<C> getCachedComponent() {
        return createdComponent.get();
    }

    @Override
    public void refreshState(EditorActionContext context) {
        // Implementations that need to closely reflect some editor state can
        // override this method anonymously.
    }

    @Override
    public void setUserObject(@Nullable Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public Optional<Object> getUserObject() {
        return Optional.ofNullable(userObject);
    }

    @Override
    public String toString() {
        return "Action: " + getLocalisedText();
    }

    public Optional<Accelerator> getAccelerator() {
        var result = new AtomicReference<Accelerator>(null);
        acceleratorId.get().ifPresent(acceleratorId -> result.set(acceleratorId.getAccelerator()));
        return Optional.ofNullable(result.get());
    }
}
