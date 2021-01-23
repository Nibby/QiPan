package yi.component.shared.component.modal;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiScene;
import yi.component.shared.component.YiStyleClass;
import yi.component.shared.utilities.SystemUtilities;

import java.util.*;

public abstract class YiAbstractModalPane implements YiModalContent {

    private final List<CloseCallback> closeCallbackList = new ArrayList<>();
    private YiScene scene;

    private final BorderPane contentRoot = new BorderPane();
    private Parent cachedContent = null;
    private ModalControlButton[] controlButtons = new ModalControlButton[0];

    public YiAbstractModalPane() {
        ModalControlButton okayButton = ModalControlButton.createOkayButton();
        setCloseTriggerButtons(okayButton);
        setDefaultControlButton(okayButton);
    }

    @Override
    public @NotNull Parent getContent() {
        if (cachedContent == null) {
            cachedContent = createContent();
            Objects.requireNonNull(cachedContent);
            contentRoot.setCenter(cachedContent);
            var bounds = cachedContent.getBoundsInLocal();
            contentRoot.setMaxSize(bounds.getWidth(), bounds.getHeight());

        }
        return contentRoot;
    }

    protected abstract @NotNull Pane createContent();

    public void setCloseTriggerButtons(ModalControlButton... buttons) {
        for (ModalControlButton button : buttons) {
            button.setParent(this);
            layoutCloseTriggerButtons(buttons);
        }
        this.controlButtons = buttons;
    }

    protected final void layoutCloseTriggerButtons(ModalControlButton[] buttons) {
        Parent newContainer = createCloseTriggerButtonsPane(buttons);
        Objects.requireNonNull(newContainer, "Close trigger buttons pane must not be null");

        Node existingContainer = contentRoot.getBottom();
        if (existingContainer != null) {
            contentRoot.getChildren().remove(existingContainer);
        }
        contentRoot.setBottom(newContainer);
    }

    /**
     * Constructs the UI component to display all {@link ModalControlButton}
     * associated with this modal pane.
     *
     * @see #setCloseTriggerButtons(ModalControlButton...)
     */
    protected @NotNull Parent createCloseTriggerButtonsPane(ModalControlButton[] buttons) {
        List<ModalControlButton> primaryButtons = new ArrayList<>();

        for (var button : buttons) {
            button.getCloseTrigger().ifPresent(trigger -> {
                if (trigger == CloseTrigger.getPrimaryTrigger()) {
                    primaryButtons.add(button);
                }
            });
        }

        var container = new HBox();
        container.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        container.getStyleClass().add(YiStyleClass.PADDING_10.getName());

        Runnable addPrimaryButtons = () -> {
            for (var button : primaryButtons) {
                container.getChildren().add(button.getNode());
            }
        };

        Runnable addSecondaryButtons = () -> {
            for (var button : buttons) {
                if (primaryButtons.contains(button)) {
                    continue;
                }
                container.getChildren().add(button.getNode());
            }
        };

        /*
            macOS desktop design guidelines recommends adding primary action furthermost
            to the right, while Windows prefers adding secondary actions to the right.
         */
        if (SystemUtilities.isMac()) {
            addPrimaryButtons.run();
            addSecondaryButtons.run();
        } else {
            addSecondaryButtons.run();
            addPrimaryButtons.run();
        }

        return container;
    }


    public void addCloseCallback(@NotNull CloseCallback callback) {
        Objects.requireNonNull(callback);
        closeCallbackList.add(callback);
    }

    public void removeCloseCallback(CloseCallback callback) {
        closeCallbackList.remove(callback);
    }

    /**
     * Optionally gives a {@link ModalControlButton} greater visual weighting than
     * other control buttons. Usually this is done to indicate the button is the
     * primary action, or the recommended action.
     *
     * @param button Optional default control button, may be null to reset existing
     *               default button.
     */
    public void setDefaultControlButton(@Nullable ModalControlButton button) {
        for (var btn : controlButtons) {
            btn.setDefaultDecorated(btn.equals(button));
        }
    }

    @Override
    public void show(@NotNull YiScene scene) {
        Objects.requireNonNull(scene);
        this.scene = scene;
    }

    @Override
    public void close(@NotNull CloseTrigger method) {
        Objects.requireNonNull(method);
        assert scene != null : "Scene should have been set when modal pane is being shown";

        closeCallbackList.forEach(callback -> callback.onPaneClosing(method));
        scene.removeModalContent(this);
    }

    public static abstract class CloseCallback {

        /**
         * Invoked when the pane is closing, just before it is removed from the scene.
         *
         * @param trigger What prompted the pane to close.
         */
        abstract void onPaneClosing(CloseTrigger trigger);

    }
}
