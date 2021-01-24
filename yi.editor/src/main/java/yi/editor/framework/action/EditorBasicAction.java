package yi.editor.framework.action;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiButton;
import yi.component.shared.component.YiMenuItem;
import yi.component.shared.i18n.TextResource;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorAccelerator;

import java.util.function.Consumer;

/**
 * Standard concrete implementation of {@link EditorAction} that represents one
 * repeatable, self-contained piece of functionality. This action is stateless.
 * <p/>
 * For stateful actions, see {@link EditorRadioAction} and
 * {@link EditorToggleAction}.
 */
public class EditorBasicAction extends EditorAbstractAction<YiMenuItem, Node> {

    public EditorBasicAction(@NotNull TextResource name) {
        this(name, null);
    }

    public EditorBasicAction(@NotNull TextResource name,
                             @Nullable Consumer<EditorActionContext> action) {
        super(name, action);
    }

    @Override
    protected @NotNull YiMenuItem getAsMenuItemImpl() {
        return new YiMenuItem(getText(), getIcon());
    }

    @Override
    protected Node getAsComponentImpl() {
        return new YiButton(getText(), getIcon());
    }

    // Methods overridden to enable co-variant return types for smoother method chaining

    @Override
    public EditorBasicAction setAccelerator(EditorAccelerator acceleratorId) {
        super.setAccelerator(acceleratorId);
        return this;
    }

    @Override
    public EditorBasicAction setInMenuBar(@NotNull EditorMainMenuType menu, double position) {
        super.setInMenuBar(menu, position);
        return this;
    }

    @Override
    public EditorBasicAction setText(@NotNull TextResource i18nKey) {
        super.setText(i18nKey);
        return this;
    }

    @Override
    public EditorBasicAction setIcon(@Nullable ImageView icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public EditorBasicAction setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public EditorBasicAction setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }
}
