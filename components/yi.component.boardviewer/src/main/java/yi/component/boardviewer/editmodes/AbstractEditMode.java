package yi.component.boardviewer.editmodes;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import yi.component.boardviewer.GameBoardManager;

import java.util.Optional;

/**
 * Edit modes represent some type of game model data manipulation. For example, one edit
 * mode works specifically with annotations on a node, while another works with adding
 * or removing stones from the game position.
 * <p/>
 * Input data is received from the canvas and interpreted to submit
 * {@link yi.component.boardviewer.edits.Undoable edits} to {@link yi.component.boardviewer.GameModelEditor}.
 * This process is the basis for the game model editing framework.
 * <p/>
 * For a list of usable edit modes, see {@link EditMode}.
 *
 * @see yi.component.boardviewer.GameModelEditor GameModel editing
 */
public abstract class AbstractEditMode {

    public abstract void renderGridCursor(GraphicsContext g, GameBoardManager manager, int gridX, int gridY);

    public Optional<Cursor> getMouseCursor() {
        return Optional.of(Cursor.HAND);
    }

    public abstract void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    public abstract void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY);

    public abstract void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY);

    public void onKeyPress(GameBoardManager manager, KeyEvent e) {

    }

}
