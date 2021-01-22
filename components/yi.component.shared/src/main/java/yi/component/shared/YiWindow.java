package yi.component.shared;

import javafx.stage.Stage;
import yi.component.shared.component.YiScene;

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
