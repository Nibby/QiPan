package yi.component.boardviewer.editmodes;

import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.boardviewer.GameBoardClassFactory;
import yi.component.boardviewer.GameBoardManagerAccessor;
import yi.component.boardviewer.edits.StoneEdit;
import yi.core.go.*;

public final class StoneEditModeMechanismTest {

    @Test
    public void testStoneEdit_NonEditNode_CreatesOne() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());

        manager.edit.recordAndApply(StoneEdit.add(null, 0, 0, StoneColor.WHITE), manager);

        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(1, model.getCurrentMoveNumber());
        Assertions.assertSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());
    }

    @Test
    public void testStoneEdit_MultipleEdits_UsesOneNode() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());

        manager.edit.recordAndApply(StoneEdit.add(null, 0, 0, StoneColor.WHITE), manager);
        // These two edits edits the current node because that's the node created. These edits should not create more nodes.
        manager.edit.recordAndApply(StoneEdit.add(model.getCurrentNode(), 1, 0, StoneColor.WHITE), manager);
        manager.edit.recordAndApply(StoneEdit.add(model.getCurrentNode(), 2, 0, StoneColor.WHITE), manager);

        Assertions.assertNotEquals(model.getRootNode(), model.getCurrentNode());
        Assertions.assertEquals(1, model.getCurrentMoveNumber());
        Assertions.assertSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());
    }

    @Test
    public void testStoneEdit_SameColorStone_RemovesIt() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        model.beginMoveSequence().playMove(0, 0);

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());

        var editMode = EditMode.editStones(StoneColor.BLACK);
        manager.edit.setEditMode(editMode);

        // Method under test
        // Edits white stone at (0, 0) which already has a move there.
        // This edit should effectively remove the stone at that position.
        editMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        // Assert
        var board = model.getCurrentGameState().getBoardPosition();
        Assertions.assertSame(StoneColor.NONE, board.getStoneColorAt(0, 0));
    }

    @Test
    public void testStoneEdit_DifferentColorStone_RemoveIt() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        model.beginMoveSequence().playMove(0, 0);

        Assertions.assertNotSame(GameNodeType.STONE_EDIT, model.getCurrentNode().getType());

        var editMode = EditMode.editStones(StoneColor.WHITE);
        manager.edit.setEditMode(editMode);

        // Method under test
        // Edits white stone at (0, 0) which already has a move there.
        // This edit should effectively remove the stone at that position.
        editMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        // Assert
        var board = model.getCurrentGameState().getBoardPosition();
        Assertions.assertSame(StoneColor.NONE, board.getStoneColorAt(0, 0));
    }

    @Test
    public void testStoneEdit_PlayedMoveColorStillCorrect() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editStoneMode = EditMode.editStones(StoneColor.BLACK);
        manager.edit.setEditMode(editStoneMode);

        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        // Playing move at stone edit node. This move is the second 'node' but is the first game move to be played
        // so it should still be a black stone.
        var playMoveMode = EditMode.playMove();
        manager.edit.setEditMode(playMoveMode);

        playMoveMode.onMousePress(MouseButton.PRIMARY, manager, 0, 1);
        Assertions.assertEquals(StoneColor.BLACK, model.getCurrentGameState().getBoardPosition().getStoneColorAt(0, 1));

        // Insert another node for stone editing.
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        // Play another game move. This move is the fourth node, but should still be a white stone because it's the
        // second game move to be played, following 'black'
        playMoveMode.onMousePress(MouseButton.PRIMARY, manager, 0, 2);
        Assertions.assertEquals(StoneColor.WHITE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(0, 2));
    }

    @Test
    public void testStoneEdit_SetupManualPosition_GameRulesUseTheseStones() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        var editStoneMode = EditMode.editStones(StoneColor.WHITE);
        manager.edit.setEditMode(editStoneMode);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 1);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 2);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 1, 0);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 1, 1);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 1, 2);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 2, 0);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 2, 1);

        var playMoveMode = EditMode.playMove();
        manager.edit.setEditMode(playMoveMode);
        // This move should capture all the white stones previously
        playMoveMode.onMousePress(MouseButton.PRIMARY, manager, 2, 2);

        // Assert the edited stones are captured.
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(0, 0));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(0, 1));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(0, 2));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(1, 0));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(1, 1));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(1, 2));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(2, 0));
        Assertions.assertEquals(StoneColor.NONE, model.getCurrentGameState().getBoardPosition().getStoneColorAt(2, 1));
        Assertions.assertEquals(StoneColor.BLACK, model.getCurrentGameState().getBoardPosition().getStoneColorAt(2, 2));
    }

    @Test
    public void testStoneEdit_onRootNode_createsNewNode() {
        var model = new GameModel(3, 3, StandardGameRules.CHINESE);
        var manager = GameBoardClassFactory.createGameBoardManager();
        GameBoardManagerAccessor.setGameModel(manager, model);

        // Sanity check
        Assertions.assertTrue(model.getRootNode().isLastMoveInThisVariation());

        var editStoneMode = EditMode.editStones(StoneColor.WHITE);
        manager.edit.setEditMode(editStoneMode);
        editStoneMode.onMousePress(MouseButton.PRIMARY, manager, 0, 0);

        Assertions.assertFalse(model.getRootNode().isLastMoveInThisVariation());
        var newNode = model.getRootNode().getNextNodeInMainBranch();
        Assertions.assertNotNull(newNode);
        Assertions.assertEquals(new Stone(0, 0, StoneColor.WHITE), newNode.getStoneEditAt(0, 0));
    }

}