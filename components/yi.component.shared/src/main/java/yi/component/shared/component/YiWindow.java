package yi.component.shared.component;

import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.modal.YiModalContent;

/**
 * An extended {@link Stage} with additional UI features.
 */
public class YiWindow {

    private final Stage stage;
    private final YiScene scene;

    public YiWindow() {
        this.stage = new Stage();
        this.scene = new YiScene();
        this.stage.setScene(scene.getScene());
    }

    /**
     * Delegates the call to {@link YiScene#setMainMenuBar(MenuBar)}.
     *
     * @param menuBar Main menu bar, nullable.
     * @see YiScene#setMainMenuBar(MenuBar)
     */
    public void setMainMenuBar(@Nullable MenuBar menuBar) {
        getScene().setMainMenuBar(menuBar);
    }

    /**
     * Delegates the call to {@link YiScene#pushModalContent(YiModalContent)}.
     *
     * @param modalContent Modal content to show.
     */
    public void pushModalContent(@NotNull YiModalContent modalContent) {
        getScene().pushModalContent(modalContent);
    }


    public void setWidth(double width) {
        this.stage.setWidth(width);
    }

    public void setHeight(double height) {
        this.stage.setHeight(height);
    }

    public double getWidth() {
        return getStage().getWidth();
    }

    public double getHeight() {
        return getStage().getHeight();
    }

    public void setMinWidth(double width) {
        getStage().setMinWidth(width);
    }

    public void setMinHeight(double height) {
        getStage().setMinHeight(height);
    }

    public void show() {
        getStage().show();
    }

    public YiScene getScene() {
        return scene;
    }

    public Stage getStage() {
        return stage;
    }
}
