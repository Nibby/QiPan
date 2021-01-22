package yi.editor.components;

import yi.core.go.GameModel;
import yi.core.go.editor.GameModelUndoSystem;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.EditorAccelerator;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;

import java.util.Optional;

public final class EditorUndoSupport implements EditorComponent<Object> {

    private final EditorAction undo;
    private final EditorAction redo;

    public EditorUndoSupport() {
        undo = new EditorBasicAction(EditorTextResources.UNDO, this::requestUndo) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var window = context.getEditorWindow();
                var model = window.getGameModel();
                var undoSystem = model.getEditor().getUndoSystem();
                undo.setEnabled(undoSystem.canUndo());
            }
        };
        undo.setInMenuBar(EditorMainMenuType.EDIT, 0d);
        undo.setAccelerator(EditorAccelerator.UNDO);
        undo.setEnabled(false);

        redo = new EditorBasicAction(EditorTextResources.REDO, this::requestRedo) {
            @Override
            public void refreshState(EditorActionContext context) {
                super.refreshState(context);
                var window = context.getEditorWindow();
                var model = window.getGameModel();
                var undoSystem = model.getEditor().getUndoSystem();
                redo.setEnabled(undoSystem.canRedo());
            }
        };
        redo.setInMenuBar(EditorMainMenuType.EDIT, 0.001d);
        redo.setAccelerator(EditorAccelerator.REDO);
        redo.setEnabled(false);
    }

    private void requestUndo(EditorActionContext context) {
        var window = context.getEditorWindow();
        var model = window.getGameModel();
        var undoSystem = model.getEditor().getUndoSystem();
        if (undoSystem.canUndo()) {
            undoSystem.performUndo();
        }
        undo.setEnabled(undoSystem.canUndo());
        redo.setEnabled(undoSystem.canRedo());
    }

    private void requestRedo(EditorActionContext context) {
        var window = context.getEditorWindow();
        var model = window.getGameModel();
        var undoSystem = model.getEditor().getUndoSystem();
        if (undoSystem.canRedo()) {
            undoSystem.performRedo();
        }
        redo.setEnabled(undoSystem.canRedo());
        undo.setEnabled(undoSystem.canUndo());
    }

    public void setGameModel(GameModel newGameModel) {
        GameModelUndoSystem undoSystem = newGameModel.getEditor().getUndoSystem();
        newGameModel.onCurrentNodeChange().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeDataUpdate().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeAdd().addListener(e -> refreshState(undoSystem));
        newGameModel.onNodeRemove().addListener(e -> refreshState(undoSystem));
    }

    private void refreshState(GameModelUndoSystem undoSystem) {

        undo.setEnabled(undoSystem.canUndo());
        redo.setEnabled(undoSystem.canRedo());
    }

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        return new EditorAction[] { undo, redo };
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }
}
