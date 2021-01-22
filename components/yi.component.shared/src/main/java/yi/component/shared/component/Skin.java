package yi.component.shared.component;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import yi.component.shared.Resource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * <p/>A skin is a collection of related CSS that is applied to JavaFx components
 * for aesthetic purposes. Each skin begins with an entry point, which is a file
 * called {@code skin.css} in the skin's top level directory.
 */
public final class Skin {

    private static final String COMMON_CSS_FILE = "/yi/component/shared/skins/common.css";
    private static final String MAIN_CSS_FILE_NAME = "skin.css";

    static {
        SkinManager.addExtraStylesheet(COMMON_CSS_FILE, Skin.class);
    }

    private final String mainCssUrl;

    private Skin(URL directoryUrl) {
        this.mainCssUrl = directoryUrl.toString() + MAIN_CSS_FILE_NAME;
    }

    private Skin(Path directory) {
        this.mainCssUrl = toMainCssUrl(directory);
    }

    private String toMainCssUrl(Path directory) {
        try {
            Path stylesheetPath = directory.resolve(MAIN_CSS_FILE_NAME);
            URI pathUri = stylesheetPath.toUri();
            URL pathUrl = pathUri.toURL();
            return pathUrl.toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Applies this skin to the given scene.
     *
     * @param scene Scene to apply skin to.
     */
    public void apply(Scene scene) {
        var mainCss = getMainCssUrl();

        ObservableList<String> stylesheets = scene.getStylesheets();

        stylesheets.add(mainCss);
        for (Resource extraStylesheet : SkinManager.getExtraStylesheets()) {
            String resourceString = extraStylesheet.getResourceUrlAsString();
            stylesheets.add(resourceString);
        }
    }


    /**
     *
     * @return URL path to the main CSS file for this skin.
     */
    String getMainCssUrl() {
        return mainCssUrl;
    }

    /**
     * Determines if a directory is a likely candidate containing skin information. The result of
     * this method is not fool-proof, as it only checks for the presence of key files, not the
     * validity of their content.
     *
     * @param directory The external file directory to test.
     * @return true if it is likely a skin directory.
     */
    public static boolean isSkinDirectory(Path directory) {
        boolean isDirectory = Files.isDirectory(directory);
        boolean hasMainCss = Files.exists(directory.resolve(MAIN_CSS_FILE_NAME));

        return isDirectory && hasMainCss;
    }

    /**
     * Instantiates a skin from an external file directory.
     *
     * @param directory Path to top level skin directory.
     * @return The skin the directory contains valid skin data, otherwise {@link Optional#empty()}.
     */
    public static Optional<Skin> fromDirectory(Path directory) {
        if (!isSkinDirectory(directory)) {
            return Optional.empty();
        }

        var skin = new Skin(directory);
        return Optional.of(skin);
    }

    /**
     * Instantiates a skin from internal resource directory.
     * The resource directory can be accessed using {@link Class#getResource(String)}.
     *
     * @param directoryUrl Resource URL of the top level skin directory.
     * @return The skin if the url points to a valid skin resource directory, otherwise {@link Optional#empty()}.
     */
    public static Optional<Skin> fromResources(String directoryUrl) {
        try {
            var url = new URL(directoryUrl);
            return Optional.of(new Skin(url));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    /**
     * Instantiates a skin from the resource loader of the specified class.
     * The resource directory can be accessed using {@link Class#getResource(String)}.
     *
     * @param directoryUrl Resource URL of the top level skin directory.
     * @return The skin if the url points to a valid skin resource directory, otherwise {@link Optional#empty()}.
     */
    public static Optional<Skin> fromResources(String directoryUrl, Class<?> resourceClass) {
        var url = resourceClass.getResource(directoryUrl);
        return Optional.of(new Skin(url));
    }
}
