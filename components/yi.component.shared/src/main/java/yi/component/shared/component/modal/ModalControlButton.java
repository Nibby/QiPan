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
public final class ModalControlButton {

    private final YiButton button;
    private CloseTrigger trigger = null;
    private YiModalContent parent = null;
    private Consumer<YiModalContent> action = null;

    public ModalControlButton(@NotNull TextResource text) {
        this(text, null);
    }

    public ModalControlButton(@NotNull TextResource text,
                              @Nullable Node icon) {
        Objects.requireNonNull(text, "Text must not be null");

        this.button = new YiButton(text, icon);
        this.button.setMinWidth(80);
        this.button.setOnAction(actionEvent -> {
            if (parent == null) {
                throw new IllegalStateException("parent == null");
            }

            if (action != null) {
                action.accept(parent);
            }

            if (trigger != null) {
                parent.close(trigger);
            }
        });
    }

    protected final void setParent(@NotNull YiModalContent modalContent) {
        Objects.requireNonNull(modalContent, "Parent must not be null");

        this.parent = modalContent;
        this.button.setOnAction(actionEvent -> modalContent.close(trigger));
    }

    protected final Optional<CloseTrigger> getCloseTrigger() {
        return Optional.ofNullable(trigger);
    }

    protected final YiButton getNode() {
        return button;
    }

    public void setActionWhenClicked(Consumer<YiModalContent> action) {
        this.action = action;
    }

    public void setCloseTrigger(CloseTrigger trigger) {
        this.trigger = trigger;
    }

    // Common button factory methods
    public static ModalControlButton createOkayButton() {
        return createOkayButton(YiPresetTextResource.BUTTON_OKAY, null);
    }

    public static ModalControlButton createOkayButton(@NotNull TextResource text,
                                                      @Nullable Node icon) {
        var button = new ModalControlButton(text, icon);
        button.setCloseTrigger(CloseTrigger.OKAY);
        return button;
    }

    public static ModalControlButton createCancelButton() {
        return createCancelButton(YiPresetTextResource.BUTTON_CANCEL, null);
    }

    public static ModalControlButton createCancelButton(@NotNull TextResource text,
                                                        @Nullable Node icon) {
        var button = new ModalControlButton(text, icon);
        button.setCloseTrigger(CloseTrigger.CANCEL);
        return button;
    }

    public void setDefaultDecorated(boolean isDefaultButton) {
        if (isDefaultButton) {
            button.getStyleClass().add(YiStyleClass.DEFAULT_BUTTON.getName());
        } else {
            button.getStyleClass().remove(YiStyleClass.DEFAULT_BUTTON.getName());
        }
    }
}
