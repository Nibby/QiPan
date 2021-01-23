package yi.editor;

import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.modal.ModalActionButton;
import yi.component.shared.component.modal.YiModalAlertPane;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.editor.GameModelEditor;
import yi.core.go.editor.edit.MoveEdit;
import yi.core.go.editor.edit.RemoveNodeEdit;
import yi.editor.components.EditorComponent;
import yi.editor.components.EditorMainMenuType;
import yi.editor.framework.EditorAccelerator;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.action.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class EditorStandardActions implements EditorComponent<Object> {

    private final ActionHandler handler;
    private final Set<EditorAction> standardActions = new HashSet<>();

    protected EditorStandardActions(@NotNull ActionHandler handler) {
        this.handler = Objects.requireNonNull(handler);

        createNewGameAction();
        createNewGameInNewWindowAction();
        createDivider(EditorMainMenuType.FILE, 0.00055);
        createOpenGameAction();
        createSaveAction();
        createSaveAsAction();

        createDivider(EditorMainMenuType.EDIT, 0.0999d);
        createPassAction();
        createDivider(EditorMainMenuType.EDIT, 0.1001d);
        createRemoveNodeAction();
    }

    private void createRemoveNodeAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.REMOVE_NODE, context -> {
            var window = context.getEditorWindow();
            GameModel gameModel = window.getGameModel();
            GameModelEditor editor = gameModel.getEditor();
            GameNode node = gameModel.getCurrentNode();
            if (!node.isRoot()) {
                if (node.isLastMoveInThisVariation()) {
                    editor.recordAndApplyUndoable(new RemoveNodeEdit(node));
                } else {
                    var modal = new YiModalAlertPane("Confirm Deletion",
                            "Deleting this move will remove all subsequent variations.\n\nWould you like to continue?");
                    var buttons = new ModalActionButton[] {
                            ModalActionButton.createOkayButton(),
                            ModalActionButton.createCancelButton()
                    };
                    modal.setControlButtons(buttons);
                    modal.setDefaultControlButton(buttons[1]); // The cancel button
                    modal.setCloseCallback(button -> {
                        if (button == buttons[0]) {
                            editor.recordAndApplyUndoable(new RemoveNodeEdit(node));
                        }
                        return true;
                    });
                    modal.setPrefSize(420, 180);
                    window.pushModalContent(modal);
                }
            }
        });
        actionItem.setInMenuBar(EditorMainMenuType.EDIT, 0.11d);
        actionItem.setAccelerator(EditorAccelerator.REMOVE_NODE);
        standardActions.add(actionItem);
    }

    private void createPassAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.PASS, context -> {
            var window = context.getEditorWindow();
            GameModel gameModel = window.getGameModel();
            GameModelEditor editor = gameModel.getEditor();
            editor.recordAndApplyUndoable(MoveEdit.Companion.pass());
        });
        actionItem.setInMenuBar(EditorMainMenuType.EDIT, 0.1d);
        actionItem.setAccelerator(EditorAccelerator.PASS);
        standardActions.add(actionItem);
    }

    private void createDivider(EditorMainMenuType menuType, double position) {
        var sep = new EditorSeparatorAction();
        sep.setInMenuBar(menuType, position);
        standardActions.add(sep);
    }

    private void createNewGameAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_NEW_GAME, handler::handleCreateNewGame);
        actionItem.setInMenuBar(EditorMainMenuType.FILE, 0d);
        actionItem.setAccelerator(EditorAccelerator.NEW_GAME);
        standardActions.add(actionItem);
    }

    private void createNewGameInNewWindowAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_NEW_WINDOW, handler::handleCreateGameInNewWindow);
        actionItem.setInMenuBar(EditorMainMenuType.FILE, 0.0005d);
        actionItem.setAccelerator(EditorAccelerator.NEW_WINDOW);

        standardActions.add(actionItem);
    }

    private void createOpenGameAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_OPEN_GAME, handler::handleOpenGame);
        actionItem.setInMenuBar(EditorMainMenuType.FILE, 0.001d);
        actionItem.setAccelerator(EditorAccelerator.OPEN_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_GAME, handler::handleSaveGame);
        actionItem.setInMenuBar(EditorMainMenuType.FILE, 0.002d);
        actionItem.setAccelerator(EditorAccelerator.SAVE_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAsAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_AS_GAME, handler::handleSaveAsGame);
        actionItem.setInMenuBar(EditorMainMenuType.FILE, 0.003d);
        actionItem.setAccelerator(EditorAccelerator.SAVE_AS_GAME);

        standardActions.add(actionItem);
    }

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        return standardActions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }

    interface ActionHandler {

        void handleCreateNewGame(EditorActionContext context);
        void handleCreateGameInNewWindow(EditorActionContext context);
        void handleOpenGame(EditorActionContext context);
        void handleSaveGame(EditorActionContext context);
        void handleSaveAsGame(EditorActionContext context);

    }
}
