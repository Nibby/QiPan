package yi.component.shared.component.modal;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiScene;
import yi.component.shared.component.YiStyleClass;
import yi.component.shared.utilities.SystemUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class YiAbstractModalPane implements YiModalContent {

    private YiScene scene;

    private final BorderPane contentRoot = new BorderPane();
    private Parent cachedContent = null;
    private ModalActionButton[] controlButtons = new ModalActionButton[0];
    private ModalActionButton defaultButton = null;
    private CloseCallback closeCallback = null;

    public YiAbstractModalPane() {
        ModalActionButton okayButton = ModalActionButton.createOkayButton();
        setControlButtons(okayButton);
        setDefaultControlButton(okayButton);

        contentRoot.getStyleClass().add(YiStyleClass.PADDING_10.getName());
    }

    @Override
    public @NotNull Parent getContent() {
        if (cachedContent == null) {
            cachedContent = createContent();
            cachedContent.getStyleClass().add(YiStyleClass.PADDING_10.getName());
            Objects.requireNonNull(cachedContent);
            contentRoot.setCenter(cachedContent);
            double prefWidth = cachedContent.prefWidth(-1);
            double height = cachedContent.prefHeight(-1);
            contentRoot.setMinSize(prefWidth, height);
            contentRoot.setMaxSize(prefWidth, height);
        }
        return contentRoot;
    }

    protected abstract @NotNull Pane createContent();

    public void setControlButtons(ModalActionButton... buttons) {
        for (ModalActionButton button : buttons) {
            button.setParent(this);
            layoutCloseTriggerButtons(buttons);
        }
        this.controlButtons = buttons;
    }

    protected final void layoutCloseTriggerButtons(ModalActionButton[] buttons) {
        Parent newContainer = createCloseTriggerButtonsPane(buttons);
        Objects.requireNonNull(newContainer, "Close trigger buttons pane must not be null");

        Node existingContainer = contentRoot.getBottom();
        if (existingContainer != null) {
            contentRoot.getChildren().remove(existingContainer);
        }
        contentRoot.setBottom(newContainer);
    }

    /**
     * Constructs the UI component to display all {@link ModalActionButton}
     * associated with this modal pane.
     *
     * @see #setControlButtons(ModalActionButton...)
     */
    protected @NotNull Parent createCloseTriggerButtonsPane(ModalActionButton[] buttons) {
        List<ModalActionButton> primaryButtons = new ArrayList<>();

        for (var button : buttons) {
            button.getCloseTrigger().ifPresent(trigger -> {
                if (trigger == ActionType.getPrimaryTrigger()) {
                    primaryButtons.add(button);
                }
            });
        }

        var container = new HBox();
        container.setSpacing(10);
        container.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Runnable addPrimaryButtons = () -> {
            for (var button : primaryButtons) {
                container.getChildren().add(button.getNode());
            }
        };

        /*
            macOS desktop design guidelines recommends adding primary action furthermost
            to the right, while Windows prefers adding secondary actions to the right.
         */
        if (SystemUtilities.isMac()) {
            addPrimaryButtons.run();
            for (int i = buttons.length - 1; i > 0; --i) {
                var button = buttons[i];
                if (primaryButtons.contains(button)) {
                    continue;
                }
                container.getChildren().add(button.getNode());
            }
        } else {
            for (var button : buttons) {
                if (primaryButtons.contains(button)) {
                    continue;
                }
                container.getChildren().add(button.getNode());
            }
            addPrimaryButtons.run();
        }

        return container;
    }


    public void setCloseCallback(@NotNull CloseCallback callback) {
        Objects.requireNonNull(callback);
        this.closeCallback = callback;
    }

    @Override
    public void executeDefaultAction() {
        if (defaultButton != null) {
            defaultButton.performAction();
        }
    }

    /**
     * Optionally gives a {@link ModalActionButton} greater visual weighting than
     * other control buttons. Usually this is done to indicate the button is the
     * primary action, or the recommended action.
     *
     * @param button Optional default control button, may be null to reset existing
     *               default button.
     */
    public void setDefaultControlButton(@Nullable ModalActionButton button) {
        this.defaultButton = null;

        for (var btn : controlButtons) {
            var defaultDecorated = btn.equals(button);
            btn.setDefaultDecorated(defaultDecorated);
            if (defaultDecorated) {
                this.defaultButton = btn;
            }
        }
    }

    @Override
    public void show(@NotNull YiScene scene) {
        Objects.requireNonNull(scene);
        this.scene = scene;
    }

    @Override
    public void close(@Nullable ModalActionButton button) {
        assert scene != null : "Scene should have been set when modal pane is being shown";

        boolean canRemove = true;
        if (closeCallback != null) {
            canRemove = closeCallback.onPaneClosing(button);
        }
        if (canRemove) {
            scene.removeModalContent(this);
        }
    }

    @FunctionalInterface
    public interface CloseCallback {

        /**
         * Invoked when the pane is closing, just before it is removed from the scene.
         * This method can cancel the close request through the return result.
         *
         * @param button Button that was clicked which prompted the pane to close.
         * @return {@code true} if the pane can close successfully, otherwise the close request
         * will be interrupted.
         */
        boolean onPaneClosing(@Nullable ModalActionButton button);

    }
}
