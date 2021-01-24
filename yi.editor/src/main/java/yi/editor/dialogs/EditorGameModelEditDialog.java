package yi.editor.dialogs;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.modal.ActionType;
import yi.component.shared.component.modal.ModalActionButton;
import yi.component.shared.component.modal.YiAbstractModalPane;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.IconUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelInfo;
import yi.core.go.StandardGameRules;
import yi.core.go.rules.GameRulesHandler;
import yi.editor.framework.EditorTextResources;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EditorGameModelEditDialog extends YiAbstractModalPane {

    private final GameModel gameModel;
    private final boolean isNewModel;

    // Essential fields
    private final Spinner<Integer> boardWidth = new Spinner<>();
    private final Spinner<Integer> boardHeight = new Spinner<>();
    private final Spinner<Integer> handicap = new Spinner<>();
    private final Spinner<Double> komi = new Spinner<>();

    private final ComboBox<GameRulesValue> gameRules = new ComboBox<>();

    // Metadata info (optional)
    private final TextField playerBlackName = new TextField();
    private final TextField playerBlackRank = new TextField();
    private final TextField playerWhiteName = new TextField();
    private final TextField playerWhiteRank = new TextField();

    private static final Map<GameRulesHandler, GameRulesValue> RULESET_TRANSLATIONS = new HashMap<>();

    static {
        RULESET_TRANSLATIONS.put(StandardGameRules.CHINESE.getRulesHandler(),
                new GameRulesValue(StandardGameRules.CHINESE, EditorTextResources.CHINESE_RULESET));
    }

    {
        boardWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 25, 19));
        boardHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 25, 19));
        handicap.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0));
        komi.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-999d, 999d, 0d, 0.5d));

        playerBlackName.setPrefWidth(146);
        playerBlackName.setPromptText("Black Name");
        playerBlackRank.setPrefWidth(50);
        playerBlackRank.setPromptText("Rank");

        playerWhiteName.setPrefWidth(146);
        playerWhiteName.setPromptText("White Name");
        playerWhiteRank.setPrefWidth(50);
        playerWhiteRank.setPromptText("Rank");

        gameRules.setPrefWidth(162);
        komi.setPrefWidth(70);
        komi.setEditable(true);
        handicap.setPrefWidth(70);
        handicap.setEditable(true);
        boardWidth.setPrefWidth(70);
        boardWidth.setEditable(true);
        boardHeight.setPrefWidth(70);
        boardHeight.setEditable(true);

        gameRules.getItems().clear();
        boolean firstIteration = true;

        for (StandardGameRules rules : StandardGameRules.values()) {
            var comboBoxValue = RULESET_TRANSLATIONS.get(rules.getRulesHandler());
            if (comboBoxValue == null) {
                // Programming oversight, all known rules should have been registered in
                // RULESET_TRANSLATIONS with an appropriate TextResource. But we also want
                // to include this new rule without crashing, just without the proper name.
                var makeshiftValue = new GameRulesValue(rules, rules.getRulesHandler().getInternalName());
                RULESET_TRANSLATIONS.put(rules.getRulesHandler(), makeshiftValue);
                comboBoxValue = makeshiftValue;
            }
            gameRules.getItems().add(comboBoxValue);
            if (firstIteration) {
                gameRules.getSelectionModel().select(0);
                firstIteration = false;
            }
        }
    }

    public final ModalActionButton actionButton = new ModalActionButton(ActionType.PRIMARY, EditorTextResources.EMPTY);
    public final ModalActionButton cancelButton = new ModalActionButton(ActionType.SECONDARY, EditorTextResources.CANCEL);

    public EditorGameModelEditDialog() {
        this.isNewModel = true;
        this.gameModel = null;
        setupActionButtons();
    }

    public EditorGameModelEditDialog(@Nullable GameModel gameModel) {
        this.gameModel = gameModel;
        this.isNewModel = false;
        setupActionButtons();

        if (gameModel != null) {
            // TODO: Need to check what happens when importing an external SGF that contain
            //       data that is outside our constraints.
            var info = gameModel.getInfo();
            playerBlackName.setText(info.getPlayerBlackName());
            playerBlackRank.setText(info.getPlayerBlackRank());
            playerWhiteName.setText(info.getPlayerWhiteName());
            playerWhiteRank.setText(info.getPlayerWhiteRank());
            handicap.getValueFactory().setValue(info.getHandicapCount());
            double komiValue = info.getKomi();
            komi.getValueFactory().setValue(komiValue);

            // Don't set:
            //  - Game rules value: we can't change that once it's created
            //    otherwise some moves permissible in one ruleset will be considered illegal
            //    in the new ruleset (i.e. NZ suicide rules -> Japanese)
            //
            //  - Board size values: can't resize board after game is created
        }
    }

    private void setupActionButtons() {
        actionButton.setText(isNewModel ? EditorTextResources.CREATE : EditorTextResources.SAVE);
        setActionButtons(actionButton, cancelButton);
        setDefaultControlButton(actionButton);
    }

    @Override
    protected @NotNull Pane createContent() {

        BorderPane container = new BorderPane();

        VBox form = new VBox(20);
        {
            GridPane playerInfoSection = new GridPane();
            playerInfoSection.setHgap(5);
            playerInfoSection.setVgap(10);
            {
                var blackIcon = new Label("", IconUtilities.loadIcon("/yi/editor/icons/blackStone_white32.png", getClass(), 24).orElse(null));
                playerInfoSection.add(blackIcon, 0, 0);
                playerInfoSection.add(playerBlackName, 1, 0);
                playerInfoSection.add(playerBlackRank, 2, 0);

                var whiteIcon = new Label("", IconUtilities.loadIcon("/yi/editor/icons/whiteStone_white32.png", getClass(), 24).orElse(null));

                playerInfoSection.add(whiteIcon, 0, 1);
                playerInfoSection.add(playerWhiteName, 1, 1);
                playerInfoSection.add(playerWhiteRank, 2, 1);
            }
            form.getChildren().add(playerInfoSection);

            GridPane essentialInfoSection = new GridPane();
            essentialInfoSection.setHgap(5);
            essentialInfoSection.setVgap(10);
            {
                var sizeLabel = new Label("Board Size:");
                GridPane.setHalignment(sizeLabel, HPos.RIGHT);
                essentialInfoSection.add(sizeLabel, 0, 0);
                essentialInfoSection.add(createBoardSizeInfoComponent(), 1, 0);

                var rulesLabel = new Label("Ruleset:");
                essentialInfoSection.add(rulesLabel, 0, 1);
                GridPane.setHalignment(rulesLabel, HPos.RIGHT);
                essentialInfoSection.add(createRulesetInfoComponent(), 1, 1);

                var komiLabel = new Label("Komi:");
                essentialInfoSection.add(komiLabel, 0, 2);
                GridPane.setHalignment(komiLabel, HPos.RIGHT);
                essentialInfoSection.add(komi, 1, 2);

                var handicapLabel = new Label("Handicap:");
                essentialInfoSection.add(handicapLabel, 0, 3);
                GridPane.setHalignment(handicapLabel, HPos.RIGHT);
                essentialInfoSection.add(handicap, 1, 3);
            }
            form.getChildren().add(essentialInfoSection);
        }
        container.setCenter(form);
        container.setPrefSize(280, 300);

        return container;
    }

    private Parent createRulesetInfoComponent() {
        if (gameModel == null) {
            return gameRules;
        } else {
            var handler = gameModel.getRules();
            var rulesetName = RULESET_TRANSLATIONS.get(handler).localizedName;
            return new Label(rulesetName);
        }
    }

    private Parent createBoardSizeInfoComponent() {
        HBox section = new HBox(5);
        if (gameModel == null) {
            var xLabel = new Label(" x ");
            HBox.setMargin(xLabel, new Insets(5, 0, 0, 0));
            section.getChildren().addAll(
                    boardWidth,
                    xLabel,
                    boardHeight
            );
        } else {
            section.getChildren().addAll(
                    new Label(String.valueOf(gameModel.getBoardWidth())),
                    new Label("x"),
                    new Label(String.valueOf(gameModel.getBoardHeight()))
            );
        }
        return section;
    }

    @Override
    public boolean isStrictModal() {
        return true;
    }

    @Override
    public boolean isContentDimmed() {
        return true;
    }

    public GameModel createNewGameModel() {
        int width = boardWidth.getValue();
        int height = boardHeight.getValue();
        GameRulesValue selectedRuleset = gameRules.getValue();

        var model = new GameModel(width, height, selectedRuleset.getRulesHandler());
        saveInfo(model.getInfo());

        return model;
    }
    public void applyChangesToGameModel() {
        if (gameModel == null) {
            throw new IllegalStateException("Attempting to apply edits to existing game " +
                    "model but it is null");
        }
        saveInfo(gameModel.getInfo());
    }

    private void saveInfo(GameModelInfo info) {
        double komiValue = komi.getValue();
        info.setKomi((float) komiValue);
        info.setHandicapCount(handicap.getValue());
        info.setPlayerBlackName(getPlayerBlackName());
        getPlayerBlackRank().ifPresent(info::setPlayerBlackRank);
        info.setPlayerWhiteName(getPlayerWhiteName());
        getPlayerWhiteRank().ifPresent(info::setPlayerWhiteRank);
    }

    private Optional<String> getPlayerWhiteRank() {
        var text = playerWhiteRank.getText();
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    private String getPlayerWhiteName() {
        return getNameOrDefault(playerWhiteName, EditorTextResources.DEFAULT_WHITE_NAME.getLocalisedText());
    }

    private Optional<String> getPlayerBlackRank() {
        var text = playerBlackRank.getText();
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    private String getPlayerBlackName() {
        return getNameOrDefault(playerBlackName, EditorTextResources.DEFAULT_BLACK_NAME.getLocalisedText());
    }

    private String getNameOrDefault(TextField nameField, String defaultNameIfTextEmpty) {
        var text = nameField.getText();
        return text.isBlank() ? defaultNameIfTextEmpty : text;
    }

    private static final class GameRulesValue {

        private final GameRulesHandler rulesHandler;
        private final String localizedName;

        public GameRulesValue(@NotNull StandardGameRules rules,
                              @NotNull TextResource translations) {
            this(rules, translations.getLocalisedText());
        }

        /**
         * For runtime fall-back handling of rules that don't have a localized name set at
         * compile-time.
         *
         * This is usually a code oversight because all rules should have a preset localized
         * name.
         *
         * @param rules Game rules this value represents
         * @param name Temporary name for this ruleset
         */
        public GameRulesValue(@NotNull StandardGameRules rules,
                              @NotNull String name) {
            this.rulesHandler = Objects.requireNonNull(rules).getRulesHandler();
            this.localizedName = name;
        }

        public GameRulesHandler getRulesHandler() {
            return rulesHandler;
        }

        @Override
        public String toString() {
            return localizedName;
        }
    }
}
