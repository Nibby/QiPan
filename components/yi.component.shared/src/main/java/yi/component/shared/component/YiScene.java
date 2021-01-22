package yi.component.shared.component;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wrapper for {@link Scene} with additional UI features. The scene is comprised
 * of the main content and a glass pane. The glass pane is used to serve modal
 * prompts and other overlay components.
 */
public final class YiScene {

    private final Scene scene;
    private final StackPane parentStack = new StackPane();

    private BorderPane content;
    private GlassPane glassPane;
    private boolean contentSet = false;

    public YiScene() {
        content = new BorderPane();
        glassPane = new GlassPane();

//        parentStack.getChildren().addAll(content, glassPane);
        scene = new Scene(content);
        SkinManager.getUsedSkin().apply(scene);
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

    public Node getContent() {
        return content.getCenter();
    }

    public boolean isContentSet() {
        return contentSet;
    }

    public void installAccelerator(KeyCombination keyCombo, Runnable runnable) {
        scene.getAccelerators().put(keyCombo, runnable);
    }

    public final Scene getScene() {
        return scene;
    }

    private static final class GlassPane extends AnchorPane {

        private GlassPane() {
            getStyleClass().add(YiStyleClass.BACKGROUND_TRANSPARENT.getName());
        }

    }
}
