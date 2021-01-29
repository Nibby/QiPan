package yi.editor.utilities;

import yi.core.go.GameModel;
import yi.core.go.StandardGameRules;
import yi.editor.framework.EditorHelper;

public final class GameModelUtilities {

    private GameModelUtilities() {
        // No instantiation
    }

    public static GameModel createGameModel() {
        return createGameModel(19, 19, StandardGameRules.CHINESE);
    }

    public static GameModel createGameModel(int width, int height, StandardGameRules ruleset) {
        return createGameModel(width, height, ruleset, ruleset.getRulesHandler().getDefaultKomi(0));
    }

    private static GameModel createGameModel(int width, int height, StandardGameRules ruleset,
                                             float customKomi) {
        var model = new GameModel(width, height, ruleset);
        model.getInfo().setKomi(customKomi);
        model.getInfo().setApplicationName(EditorHelper.getProgramName() + " " + EditorHelper.getVersion());

        return model;
    }

}
