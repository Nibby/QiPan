package yi.editor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import yi.core.go.*;
import yi.core.go.docformat.FileFormat;
import yi.editor.components.EditorPerspective;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.action.EditorActionContext;
import yi.editor.utilities.GameModelUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

final class EditorStandardActionsHandler implements EditorStandardActions.ActionHandler {

    protected EditorStandardActionsHandler() {
        // Make inaccessible to exported API
    }

    @Override
    public void handleCreateNewGame(EditorActionContext context) {
        var window = context.getEditorWindow();
        // TODO: Show a new dialog prompting for new game document information.
        //       The values below are hard-coded, and are temporary.
        var existingModel = window.getGameModel();
        var doIt = new AtomicBoolean(false);

        if (existingModel.isModified()) {
            var overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Would you like to save your changes to the current document?",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            overwriteAlert.showAndWait().ifPresent(selectedButton -> {
                if (selectedButton == ButtonType.CANCEL) {
                    doIt.set(false);
                } else if (selectedButton == ButtonType.YES) {
                    promptAndStoreSaveFile(existingModel, window);
                    doIt.set(true);
                } else {
                    doIt.set(true);
                }
            });
        } else {
            doIt.set(true);
        }

        if (doIt.get()) {
            var newModel = new GameModel(19, 19, StandardGameRules.CHINESE);
            window.setGameModel(newModel);
        }
    }

    @Override
    public void handleCreateGameInNewWindow(EditorActionContext context) {
        var newWindow = new EditorWindow(GameModelUtilities.createGameModel(), EditorPerspective.REVIEW);
        newWindow.show();
    }

    @Override
    public void handleOpenGame(EditorActionContext context) {
        var window = context.getEditorWindow();
        var fileChooser = new FileChooser();
        fileChooser.setTitle(EditorTextResources.MENUITEM_OPEN_GAME.getLocalisedText());
        File selectedFile = fileChooser.showOpenDialog(window.getStage());
        if (selectedFile != null) {
            try {
                var importedModel = GameModelImporter.INSTANCE.fromFile(selectedFile.toPath());
                window.setGameModel(importedModel);
            } catch (GameParseException | IOException e) {
                // TODO: Error handling
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleSaveGame(EditorActionContext context) {
        var window = context.getEditorWindow();
        var existingModel = window.getGameModel();
        Path saveFilePath = existingModel.getLastSavePath();

        if (saveFilePath == null) {
            saveFilePath = promptAndStoreSaveFile(existingModel, window);
        }

        if (saveFilePath != null && existingModel.getLastSaveFormat() != null) {
            GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, existingModel.getLastSaveFormat());
        }
    }

    @Override
    public void handleSaveAsGame(EditorActionContext context) {
        var window = context.getEditorWindow();
        var existingModel = window.getGameModel();
        Path saveFilePath = promptAndStoreSaveFile(existingModel, window);
        FileFormat saveFileFormat = existingModel.getLastSaveFormat();

        if (saveFilePath != null) {
            GameModelExporter.INSTANCE.toFile(existingModel, saveFilePath, saveFileFormat);
        }
    }

    /*
        TODO:
            1. Wrapper around FileFormat to provide an ExtensionFilter for FileChooser for
               save/load
            2. For now we're assuming the format being saved is SGF, but we need to determine
               this from the selected extension filter.
     */
    private static Path promptAndStoreSaveFile(GameModel gameModel, EditorWindow window) {
        var saveFileChooser = new FileChooser();
        saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Smart Game Format (*.sgf)", "sgf"));
        saveFileChooser.setTitle("Select Save Location");
        File saveFile = saveFileChooser.showSaveDialog(window.getStage());
        if (saveFile != null) {
            Path savePath = saveFile.toPath();
            gameModel.setLastSavePath(savePath);
            gameModel.setLastSaveFormat(FileFormat.SGF);
            return savePath;
        }
        return null;
    }

}
