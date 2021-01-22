package yi.editor;

import javafx.scene.control.Menu;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;
import yi.editor.components.EditorMainMenuType;
import yi.editor.components.EditorPerspective;
import yi.editor.framework.EditorHelper;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.utilities.GameModelUtilities;

import java.util.Optional;

@ExtendWith(ApplicationExtension.class)
public abstract class EditorUITestBase {

    @BeforeAll
    public static void initializeTestEnvironment() {
        EditorHelper.setRunningAsTest(true);
        EditorHelper.setUseSystemMenuBar(false); // So that testFx can find the menu components...
    }

    protected EditorWindow window;
    protected GameModel gameModel;

    @Start
    public void startFx(Stage stage) {
        EditorHelper.initializeContext(null);

        gameModel = GameModelUtilities.createGameModel(19, 19, StandardGameRules.CHINESE);
        window = new EditorWindow(gameModel, EditorPerspective.COMPACT) {
            @Override
            protected void initializeActions(EditorActionManager actionManager) {
                super.initializeActions(actionManager);
                initializeTestActions(actionManager);
            }
        };
        stage = window.getStage();
        stage.show();
        stage.requestFocus();
    }

    protected abstract void initializeTestActions(EditorActionManager manager);

    @AfterEach
    public void dispose() {
        gameModel.dispose();
        System.gc();
    }

    public EditorWindow getFrame() {
        return window;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    /**
     * Gets the {@link Menu} component in the {@link yi.editor.components.EditorMenuBar}
     * that corresponds to the given menu type. If it is not present, returns
     * {@link Optional#empty()}.
     *
     * @param menuType Menu type to retrieve menu for.
     * @return The corresponding menu, if it exists.
     */
    public Optional<Menu> getMenu(EditorMainMenuType menuType) {
        if (window != null) {
            return window.getMainMenuBar().getMenus().stream()
                    .filter(menu -> menu.getUserData() == menuType)
                    .findFirst();
        }
        return Optional.empty();
    }
}
