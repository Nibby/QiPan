package yi.component.shared.component;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.modal.CloseTrigger;
import yi.component.shared.component.modal.YiModalContent;

import java.util.Objects;
import java.util.Stack;

/**
 * Wrapper for {@link Scene} with additional UI features. The scene is comprised
 * of the main content and a glass pane. The glass pane is used to serve modal
 * prompts and other overlay components.
 */
public final class YiScene {

    private final Scene scene;
    private final BorderPane sceneRoot = new BorderPane();
    private final StackPane parentStack = new StackPane();

    private final BorderPane content;
    private final GlassPane glassPane;
    private boolean contentSet = false;
    
    private final Stack<YiModalContent> modalContentStack = new Stack<>();

    public YiScene() {
        content = new BorderPane();
        glassPane = new GlassPane();

        parentStack.getChildren().addAll(content);
        sceneRoot.setCenter(parentStack);
        scene = new Scene(sceneRoot);
        SkinManager.getUsedSkin().apply(scene);
    }

    /**
     * Adds one model content component to be shown. This will block input to the
     * main content pane until the model content is dismissed.
     *
     * If another modal content is showing, this will immediately override the existing
     * content and show the new content. The old content will be shown once this content
     * is dismissed.
     *
     * @param modalContent Modal content to show.
     */
    public void pushModalContent(@NotNull YiModalContent modalContent) {
        Objects.requireNonNull(modalContent, "Modal content must not be null");
        final boolean isAnimated = true;

        modalContent.show(this);
        modalContentStack.push(modalContent);
        setGlassPaneVisible(true);
        if (glassPane.hasContent()) {
            glassPane.clearContent(glassPane.currentModalContent, false, null);
        }
        glassPane.setContent(modalContent, isAnimated);
    }

    public void removeModalContent(@NotNull YiModalContent contentToRemove) {
        Objects.requireNonNull(contentToRemove, "Modal content must not be null");
        assert modalContentStack.size() > 0 : "Called removeModalContent() when no modal items exist";

        boolean updateGlassPaneContents = contentToRemove.equals(modalContentStack.peek());
        modalContentStack.remove(contentToRemove);

        if (updateGlassPaneContents) {
            glassPane.clearContent(contentToRemove, true, () -> {
                if (modalContentStack.size() > 0) {
                    var nextContent = modalContentStack.peek();
                    glassPane.setContent(nextContent, false);
                } else {
                    setGlassPaneVisible(false);
                }
            });
        }
    }

    private void setGlassPaneVisible(boolean isVisible) {
        ObservableList<Node> parentStackContents = parentStack.getChildren();

        if (isVisible) {
            if (!parentStackContents.contains(glassPane)) {
                parentStackContents.add(glassPane);
            }
        } else {
            glassPane.setBackgroundDimmed(false, () -> parentStackContents.remove(glassPane));
        }
    }

    /**
     * Sets the main content to be displayed in this scene.
     *
     * This method will not affect the glass pane state.
     *
     * @param contentRoot Root container for the content to show.
     */
    public void setContent(@NotNull Parent contentRoot) {
        Objects.requireNonNull(contentRoot, "Content root must not be null");
        content.getChildren().clear();
        content.setCenter(contentRoot);
        contentSet = true;
    }

    /**
     * @return Current main content (non-modal) set by {@link #setContent(Parent)}.
     */
    public Node getContent() {
        return content.getCenter();
    }

    /**
     * @return {@code true} if {@link #setContent(Parent)} has been called at least once.
     */
    public boolean isContentSet() {
        return contentSet;
    }

    /**
     * Adds an accelerator trigger to this scene.
     *
     * @param keyCombo Key combination to register.
     * @param runnable Task to perform when the key combination is active.
     */
    public void installAccelerator(KeyCombination keyCombo, Runnable runnable) {
        scene.getAccelerators().put(keyCombo, runnable);
    }

    /**
     * @return The underlying {@link Scene} contained by this class.
     */
    public final Scene getScene() {
        return scene;
    }

    /**
     * Sets the primary menu bar for this window. This will override the previously
     * set menu bar.
     *
     * The menu bar may be null to erase existing menu bar.
     *
     * @param menuBar Primary menu bar, nullable.
     */
    public void setMainMenuBar(@Nullable MenuBar menuBar) {
        Node existingMenuBar = sceneRoot.getTop();
        if (existingMenuBar != null) {
            sceneRoot.getChildren().remove(existingMenuBar);
        }
        if (menuBar != null) {
            sceneRoot.setTop(menuBar);
        }
    }

    /*
        Manages the modal content for the scene. The glass pane is usually not added to
        the parent stack because its existence interferes with input events on the main
        content.
     */
    private static final class GlassPane extends BorderPane {

        private static final int BACKGROUND_DIM_ANIMATION_DURATION = 350;
        private static final int CONTENT_ANIMATION_DURATION = 350;

        private final StackPane contentStack = new StackPane();
        private final BorderPane dimPane = new BorderPane();

        private YiModalContent currentModalContent = null;
        private Parent currentContentRoot = null;

        private GlassPane() {
            setCenter(contentStack);
            contentStack.getChildren().add(dimPane);

            dimPane.getStyleClass().add(YiStyleClass.BACKGROUND_BLACK_60_PERCENT.getName());
            getStyleClass().add(YiStyleClass.BACKGROUND_TRANSPARENT.getName());
            dimPane.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressInBackground);

            setBackgroundDimmed(false, null);
        }

        private void onMousePressInBackground(MouseEvent evt) {
            if (currentModalContent != null && !currentModalContent.isStrictModal()) {
                currentModalContent.close(CloseTrigger.CANCEL);
            }
        }

        /**
         * Sets the content to be shown on the glass pane. This method supports animation
         * playback on the component being added. The animation is non-blocking on the caller
         * thread.
         *
         * @param modalContent Content to set
         * @param isNewContent {@code true} if the content is new to the glass pane and
         *                     should be animated. Otherwise if the content already exists
         *                     on the modal content stack and is being shown again
         *                     (because a more recent modal content was shown earlier and
         *                     dismissed), use {@code false}.
         */
        public void setContent(YiModalContent modalContent, boolean isNewContent) {
            resetContentStack();
            if (isNewContent) {
                setBackgroundDimmed(modalContent.isContentDimmed(), () -> {});
            }

            Parent modalContentRoot = modalContent.getContent();

            if (isNewContent) {
                decorateContentRoot(modalContentRoot);
            }

            StackPane.setAlignment(modalContentRoot, Pos.CENTER);
            contentStack.getChildren().add(modalContentRoot);
            currentModalContent = modalContent;
            currentContentRoot = modalContentRoot;

            if (isNewContent) {
                animateContentSet(modalContentRoot);
            }
        }

        /**
         * Erases all content on the glass pane, preserving only the background
         * dim panel. This method supports animation playback on the component being
         * removed. The animation is non-blocking on the caller thread.
         *
         * @param content Content to be removed
         * @param animated Whether this removal should be animated
         * @param onAnimationFinished Code to execute once the removal animation has
         *                            completed. Nullable.
         */
        public void clearContent(@NotNull YiModalContent content,
                                 boolean animated,
                                 @Nullable Runnable onAnimationFinished) {
            Objects.requireNonNull(content);
            Runnable clearTask = () -> {
                resetContentStack();
                if (onAnimationFinished != null) {
                    onAnimationFinished.run();
                }
            };

            if (content == currentModalContent) {
                if (animated) {
                    animateContentClear(currentContentRoot, clearTask);
                } else {
                    clearTask.run();
                }

                currentContentRoot = null;
                currentModalContent = null;
            } else {
                throw new IllegalArgumentException("Content being removed != content shown");
            }
        }

        private void resetContentStack() {
            for (int i = 0; i < contentStack.getChildren().size();) {
                var child = contentStack.getChildren().get(i);
                if (!child.equals(dimPane)) {
                    contentStack.getChildren().remove(i);
                } else {
                    ++i;
                }
            }
        }

        private void animateContentClear(@NotNull Parent content,
                                         @Nullable Runnable removalTask) {
            Objects.requireNonNull(content);

            var fade = new FadeTransition(Duration.millis(CONTENT_ANIMATION_DURATION), content);
            fade.setFromValue(1.0d);
            fade.setToValue(0d);
            fade.setCycleCount(1);
            fade.setAutoReverse(false);

            var slideDown = new TranslateTransition(Duration.millis(CONTENT_ANIMATION_DURATION), content);
            slideDown.setToY(contentStack.getHeight() / 3 * 2);
            slideDown.setAutoReverse(false);
            slideDown.setCycleCount(1);

            var compositeAnimation = new ParallelTransition(fade, slideDown);
            compositeAnimation.setCycleCount(1);
            if (removalTask != null) {
                compositeAnimation.setOnFinished(evt -> removalTask.run());
            }
            compositeAnimation.play();
        }

        private void animateContentSet(Parent modalContentRootComponent) {
            var fade = new FadeTransition(Duration.millis(CONTENT_ANIMATION_DURATION),
                    modalContentRootComponent);
            fade.setFromValue(0.0d);
            fade.setToValue(1.0d);
            fade.setCycleCount(1);
            fade.setAutoReverse(false);

            var slideUp = new TranslateTransition(Duration.millis(CONTENT_ANIMATION_DURATION),
                    modalContentRootComponent);
            slideUp.setFromY(contentStack.getHeight() / 3 * 2);
            slideUp.setToY(modalContentRootComponent.getLayoutY());
            slideUp.setAutoReverse(false);
            slideUp.setCycleCount(1);

            var compositeAnimation = new ParallelTransition(fade, slideUp);
            compositeAnimation.setCycleCount(1);
            compositeAnimation.play();
        }

        private void decorateContentRoot(Parent contentRoot) {
            contentRoot.getStyleClass().addAll(
                YiStyleClass.MODAL_CONTENT_CONTAINER.getName()
            );

            var shadow = new DropShadow(50d, new Color(0.1d, 0.1d, 0.1d, 0.75d));
            shadow.setBlurType(BlurType.GAUSSIAN);
            shadow.setOffsetY(10d);
            contentRoot.setEffect(shadow);
        }

        private void setBackgroundDimmed(boolean isDimmed, @Nullable Runnable onAnimationFinish) {
            if (dimPane.isVisible() != isDimmed) {
                dimPane.setVisible(isDimmed);

                // Animation
                double from;
                double to;

                if (isDimmed) {
                    from = 0.0d;
                    to = 1.0d;
                } else {
                    from = 1.0d;
                    to = 0.0d;
                }

                var animation = new FadeTransition(Duration.millis(BACKGROUND_DIM_ANIMATION_DURATION), dimPane);
                animation.setFromValue(from);
                animation.setToValue(to);
                animation.setCycleCount(1);
                animation.setAutoReverse(false);
                if (onAnimationFinish != null) {
                    animation.setOnFinished(evt -> onAnimationFinish.run());
                }
                animation.play();
            }
        }

        public boolean hasContent() {
            return currentModalContent != null;
        }
    }
}
