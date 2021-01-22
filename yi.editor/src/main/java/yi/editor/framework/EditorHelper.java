package yi.editor.framework;

import javafx.application.Application;
import javafx.scene.text.Font;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.audio.CommonAudioSets;
import yi.component.shared.audio.SoundManager;
import yi.component.shared.component.FontManager;
import yi.component.shared.component.SkinManager;
import yi.component.shared.component.YiScene;
import yi.component.shared.utilities.SystemUtilities;
import yi.editor.settings.EditorSettings;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Global helper class that manages various startup configurations for the editor
 * application.
 */
public final class EditorHelper {

    private static final Version VERSION = new Version(0, 0, 1, "alpha");
    private static final String PROGRAM_NAME = "Yi";

    private static boolean useSystemMenuBar = SystemUtilities.isMac();
    private static boolean runningAsTest = false;
    private static Path preferredSettingsRootPath = null;
    private static boolean isInitialized = false;

    private EditorHelper() {
        // Utility class, no instantiation
    }

    /**
     * @return Version of this editor application.
     */
    public static Version getVersion() {
        return VERSION;
    }

    /**
     * @return Name of this editor application.
     */
    public static String getProgramName() {
        return PROGRAM_NAME;
    }

    /**
     * @return {@code true} if the application is currently run within an IDE rather than
     * a distribution.
     */
    public static boolean isRunningFromSource() {
        return System.getProperty("yi.fromSource", "false").equalsIgnoreCase("true");
    }

    /**
     * In debug mode, components are expected to display additional technical information
     * reserved for development purposes.
     *
     * @return {@code true} if debug mode is to be enabled.
     */
    public static boolean isDebugMode() {
        return !isRunningFromSource();
    }

    /**
     * @param isRunningAsTest {@code true} if the application is being run as part of a
     * test suite.
     */
    public static synchronized void setRunningAsTest(boolean isRunningAsTest) {
        EditorHelper.runningAsTest = isRunningAsTest;
    }

    /**
     * @return {@code true} if the application is being run as part of a test suite.
     */
    public static boolean isRunningAsTest() {
        return runningAsTest;
    }

    /**
     * Set whether the application menu bar should be displayed in a manner that appears
     * native to the current operating system. For example on macOS, if this is enabled,
     * the menu bar will appear on the top of the screen rather than a custom JavaFx
     * component in the window itself.
     *
     * @param yes Whether to use system native menu bar appearance.
     */
    public static void setUseSystemMenuBar(boolean yes) {
        useSystemMenuBar = yes;
    }

    /**
     * @return {@code true} if using system native menu bar appearance.
     */
    public static boolean isUsingSystemMenuBar() {
        return useSystemMenuBar;
    }

    /**
     * Sets a path to save settings data, which will override the default settings path
     * determined by {@link EditorSettings} when the application starts up.
     *
     * @param path Settings data override path.
     */
    public static synchronized void setPreferredSettingsRootPath(@Nullable Path path) {
        EditorHelper.preferredSettingsRootPath = path;
    }

    /**
     * @return Settings data override path.
     * @see #setPreferredSettingsRootPath(Path)
     */
    public static Optional<Path> getPreferredSettingsRootPath() {
        return Optional.ofNullable(preferredSettingsRootPath);
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Initializes various core components as part of the startup procedure, before
     * any {@link yi.editor.EditorWindow} is created.
     *
     * This step should only be performed once upon startup.
     */
    public static synchronized void initializeContext(@Nullable Application.Parameters parameters) {
        if (!isRunningAsTest()) {
            EditorUncaughtExceptionHandler.initialize();
        }
        if (isInitialized && !isRunningAsTest()) {
            throw new IllegalStateException("initializeContext() should only be called " +
                    "once in production");
        }
        EditorApplicationEventHandler.initialize(parameters);
        EditorFontManager.loadBundledFonts();
        SkinManager.addExtraStylesheet("/yi/editor/fonts/font.css", EditorHelper.class);
        FontManager.setDefaultFont(new Font("Noto Sans", 12d));
        EditorTextResources.installSupportedLanguages();
//        I18n.setCurrentLanguage(Language.getSupportedLanguages().get(1));
        SkinManager.useDefaultSkin();
        EditorSettings.load();

        if (!isRunningAsTest()) {
            SoundManager.load(CommonAudioSets.Stones.CERAMIC_BICONVEX); // TODO: Temporary value, extract this to a setting
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EditorSettings.general.save();
            EditorSettings.accelerator.save();
        }));

        isInitialized = true;

        if (!isRunningAsTest()) {
            EditorApplicationEventHandler.loadAllQueuedOpenFiles();
        }
    }
}
