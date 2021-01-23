package yi.component.shared.component.modal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiButton;
import yi.component.shared.component.YiStyleClass;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.internal.YiPresetTextResource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A special button on the footer section of a {@link YiAbstractModalPane} that executes
 * some action in the context of the modal dialog. Examples include 'OK', 'Cancel' or
 * 'Apply' buttons.
 */
public final class ModalActionButton {

    private final YiButton button;
    private final ActionType actionType;
    private YiModalContent parent = null;
    private Consumer<YiModalContent> action = null;

    public ModalActionButton(@NotNull ActionType actionType,
                             @NotNull TextResource text) {
        this(actionType, text, null);
    }

    public ModalActionButton(@NotNull ActionType actionType,
                             @NotNull TextResource text,
                             @Nullable Node icon) {
        Objects.requireNonNull(text, "Text must not be null");
        Objects.requireNonNull(actionType, "Action type must not be null");

        this.actionType = actionType;
        this.button = new YiButton(text, icon);
        this.button.setMinWidth(80);
        this.button.setOnAction(actionEvent -> {
            if (parent == null) {
                throw new IllegalStateException("parent == null");
            }

            if (action != null) {
                action.accept(parent);
            }

            parent.close(this);
        });
    }

    protected final void setParent(@NotNull YiModalContent modalContent) {
        Objects.requireNonNull(modalContent, "Parent must not be null");

        this.parent = modalContent;
    }

    protected final Optional<ActionType> getCloseTrigger() {
        return Optional.ofNullable(actionType);
    }

    protected final YiButton getNode() {
        return button;
    }

    public void setActionWhenClicked(Consumer<YiModalContent> action) {
        this.action = action;
    }

    // Common button factory methods
    public static ModalActionButton createOkayButton() {
        return createPrimaryButton(YiPresetTextResource.BUTTON_OKAY, null);
    }

    public static ModalActionButton createPrimaryButton(@NotNull TextResource text,
                                                     @Nullable Node icon) {
        return new ModalActionButton(ActionType.PRIMARY, text, icon);
    }

    public static ModalActionButton createCancelButton() {
        return createSecondaryButton(YiPresetTextResource.BUTTON_CANCEL, null);
    }

    public static ModalActionButton createSecondaryButton(@NotNull TextResource text,
                                                          @Nullable Node icon) {
        return new ModalActionButton(ActionType.SECONDARY, text, icon);
    }

    public void setDefaultDecorated(boolean isDefaultButton) {
        if (isDefaultButton) {
            button.getStyleClass().add(YiStyleClass.DEFAULT_BUTTON.getName());
        } else {
            button.getStyleClass().remove(YiStyleClass.DEFAULT_BUTTON.getName());
        }
    }
}
