package yi.component.treeviewer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.MoveValidationResult;
import yi.core.go.rules.GameRulesHandler;

import java.util.concurrent.TimeUnit;

// TODO: Move me into a performance test source set
public class GameTreeStructurePerformanceTest {

    @Test
    public void testVerticalGrowth_300Nodes_FastEnough() {
        testPerformance(createModelWithMainVariation(300), 10);
    }

    @Test
    public void testVerticalGrowth_600Nodes_FastEnough() {
        testPerformance(createModelWithMainVariation(600), 10);
    }

    @Test
    public void testVerticalGrowth_1200Nodes_FastEnough() {
        testPerformance(createModelWithMainVariation(1200), 20);
    }

    @Test
    public void testVerticalGrowth_3000Nodes_FastEnough() {
        testPerformance(createModelWithMainVariation(3000), 30);
    }

    @Test
    public void testManyBranches_300Branches_FastEnough() {
        var model = createModelWithMainVariation(400);

        // Create a branch of size 1 on each node in the main variation
        for (int i = 0; i < 300; ++i) {
            playMoveSomewhereVacant(model);
            model.toPreviousNode();
            model.toNextNode();
        }

        testPerformance(model, 30);
    }

    @Test
    public void testManyBranches_600Branches_FastEnough() {
        var model = createModelWithMainVariation(300);

        for (int i = 0; i < 300; ++i) {
            playMoveSomewhereVacant(model);
            playMoveSomewhereVacant(model);
            model.toPreviousNode();
            playMoveSomewhereVacant(model);
            model.toPreviousNode(2);
            model.toNextNode();
        }

        testPerformance(model, 80);
    }

    @Test
    public void testManyBranches_1200Branches_FastEnough() {
        var model = createModelWithMainVariation(300);

        for (int i = 0; i < 400; ++i) {
            playMoveSomewhereVacant(model);
            playMoveSomewhereVacant(model);
            playMoveSomewhereVacant(model);
            model.toPreviousNode(2);
            playMoveSomewhereVacant(model);
            model.toPreviousNode();
            model.toNextNode();
            playMoveSomewhereVacant(model);
            model.toPreviousNode(3);
        }

        testPerformance(model, 160);
    }

    @Test
    public void testManyBranches_3000Branches_FastEnough() {
        var model = createModelWithMainVariation(500);

        for (int i = 0; i < 500; ++i) {

            // Create six branches for every node in the main variation
            for (int j = 0; j < 6; ++j) {
                playMoveSomewhereVacant(model);
                playMoveSomewhereVacant(model);
                playMoveSomewhereVacant(model);
                playMoveSomewhereVacant(model);
                playMoveSomewhereVacant(model);
                model.toPreviousNode(4);
            }

            model.toPreviousNode(6);
            model.toNextNode();
        }

        testPerformance(model, 400);
    }

    private void playMoveSomewhereVacant(GameModel model) {
        int w = model.getBoardWidth();
        int h = model.getBoardHeight();

        var nextNodes = model.getCurrentNode().getChildNodes();

        for (int x = 0; x < w; x++) {
            seekNext:
            for (int y = 0; y < h; y++) {

                // Ensure the move we play will definitely create a new node instead of re-using an existing one
                // because another child has already played there.
                for (GameNode nextNode : nextNodes) {
                    if (nextNode.getPrimaryMove() != null) {
                        var move = nextNode.getPrimaryMove();
                        if (move != null && move.getY() == y && move.getX() == x) {
                            continue seekNext;
                        }
                    }
                }

                boolean success = model.getEditor().addMove(x, y).getValidationResult() == MoveValidationResult.OK;

                if (success) {
                    return;
                }
            }
        }
    }

    private void testPerformance(GameModel model, long expectedDurationMillis) {
        // Warm up the VM by doing a few dry runs.
        // Most of the time the fresh start is slower than subsequent attempts because of initialization overhead
        for (int i = 0; i < 3; ++i) {
            var structure = new GameTreeStructure();
            structure.setGameModel(model);
        }

        long average = 0;
        int trials = 5;

        for (int i = 0; i < trials; ++i) {
            long startTime = System.nanoTime();
            var structure = new GameTreeStructure();
            structure.setGameModel(model);
            long endTime = System.nanoTime();

            long timeElapsed = endTime - startTime;
            long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(Math.round(timeElapsed));
            average += timeElapsedMillis;
        }

        long averageTime = average / trials;

        Assertions.assertTrue(averageTime <= expectedDurationMillis,
                "Averaged " + averageTime + "ms out of " + trials + " runs to construct tree structure, expected " + expectedDurationMillis + "ms");
    }

    private GameModel createModelWithMainVariation(int nodeCount) {
        int boardWidth = (int) Math.round(Math.sqrt(nodeCount) + 1);
        int boardHeight = (int) Math.round(Math.sqrt(nodeCount) + 1);

        var model = new GameModel(boardWidth, boardHeight, new TestingRules());

        int x = 0;
        int y = 0;

        for (int i = 0; i < nodeCount; ++i) {
            model.beginMoveSequence()
                    .playMove(x, y);

            x++;
            if (x >= boardWidth) {
                x = 0;
                y++;

                if (y >= boardHeight) {
                    throw new IllegalStateException("Unexpected number of vertical nodes (too many)!");
                }
            }
        }

        model.toPreviousNode(boardWidth * boardHeight - 1);
        return model;
    }

    private static final class TestingRules extends GameRulesHandler {

        @Override
        public float getDefaultKomi() {
            return 0;
        }

        @Override
        public boolean allowSuicideMoves() {
            return false;
        }

        @NotNull
        @Override
        public String getInternalName() {
            return "Testing";
        }
    }
}
