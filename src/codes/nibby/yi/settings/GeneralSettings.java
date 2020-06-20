package codes.nibby.yi.settings;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

final class GeneralSettings extends SettingsModule {

    private final String settingsFile;

    private static final String KEY_BOARD_THEME_DIRECTORY = "boardTheme";

    // The theme directory to be loaded
    private String selectedBoardThemeDirectory;

    public GeneralSettings(String settingsFilePath) {
        this.settingsFile = settingsFilePath;
    }

    @Override
    public void load() {
        try {
            Optional<JSONObject> settingsWrapper = Settings.readJSON(settingsFile);
            if (settingsWrapper.isEmpty()) {
                useAndSaveDefaults();
            } else {
                loadFromJson(settingsWrapper.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromJson(JSONObject settings) {
        selectedBoardThemeDirectory = settings.getString(KEY_BOARD_THEME_DIRECTORY);
    }

    @Override
    public void save() {

    }

    private void useAndSaveDefaults() {
        setSelectedBoardThemeDirectory(GameBoardThemeSettings.getDefaultThemeDirectory());
        save();
    }

    public String getSelectedBoardTheme() {
        return selectedBoardThemeDirectory;
    }

    public void setSelectedBoardThemeDirectory(String selectedBoardThemeDirectory) {
        this.selectedBoardThemeDirectory = selectedBoardThemeDirectory;
    }
}