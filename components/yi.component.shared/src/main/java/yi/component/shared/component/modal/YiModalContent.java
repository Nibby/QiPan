package yi.component.shared.component.modal;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiScene;

/**
 *
 */
public interface YiModalContent {

    /**
     * Returns the top-level container for all the components to show in this modal
     * content. This method may be invoked multiple times as newer modal content is
     * pushed to the stack then removed, so implementation must ensure the returned
     * container reference is the same for each call -- that is, it is not creating a
     * new component each time.
     *
     * @return Modal content component container.
     */
    @NotNull Parent getContent();

    /**
     * For most modal content, it is recommended to return {@code false} to avoid making
     * users feel trapped in the context.
     *
     * @return {@code true} if strict, which means this modal content cannot be dismissed
     * by clicking outside of content boundaries.
     */
    boolean isStrictModal();

    /**
     * Determines whether the main content underneath the glass pane should be
     * coated with a layer of semi-transparent black when this modal content is showing.
     *
     * @return {@code true} if the background content should be dimmed.
     */
    boolean isContentDimmed();

    /**
     * Signal that this modal content is now being shown on screen.
     *
     * @param scene The scene the modal content is added too.
     */
    void show(@NotNull YiScene scene);

    /**
     * Signal that this modal content should be dismissed.
     *
     * @param selectedActionButton Button that was selected which triggered the close action.
     *                             This may be null, which means the panel was dismissed
     *                             without a button press.
     */
    void close(@Nullable ModalActionButton selectedActionButton);

    /**
     * Requests any default (recommended) action in the modal content be executed. If the execution
     * is successful, the implementation must close the modal content. For dialog actions, this
     * method should be equivalent to clicking the 'OK' button.
     */
    void executeDefaultAction();

}
