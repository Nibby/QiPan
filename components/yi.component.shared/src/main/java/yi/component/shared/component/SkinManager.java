package yi.component.shared.component;

import yi.component.shared.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks all preset component {@link Skin} available to the platform. Each
 * skin contains CSS files specific to the
 * type of aesthetics it aims to achieve.
 *
 * <p/>Additional CSS may be added during runtime to be used by all skins, see
 * {@link #addExtraStylesheet(String, Class)}.
 */
public final class SkinManager {

    private static final Set<Resource> EXTRA_STYLESHEETS = new HashSet<>();

    private static final Skin SLATE = Skin.fromResources("/yi/component/shared/skins/slate/", SkinManager.class).orElseThrow();

    private static final AtomicReference<Skin> USED_SKIN = new AtomicReference<>(null);

    private SkinManager() { }

    public static void useDefaultSkin() {
        useSkin(SLATE);
    }

    /**
     * Selects the skin to be applied to Yi components.
     *
     * @param skin Skin to use.
     */
    public static void useSkin(Skin skin) {
        Objects.requireNonNull(skin);
        USED_SKIN.set(skin);
    }

    /**
     * @return Selected skin to be applied.
     */
    public static Skin getUsedSkin() {
        return USED_SKIN.get();
    }

    /**
     * Adds one additional CSS file entry to be used by all skins when applied to the
     * {@link YiWindow}. The file will be loaded using the class loader
     * in the module of the CSS file.
     *
     * @param cssResourcePath Internal resource path for the CSS file.
     * @param resourceLoaderClass Class loader to use to load the resource. This class should
     *                            be in the same module as the CSS file.
     */
    public static void addExtraStylesheet(String cssResourcePath, Class<?> resourceLoaderClass) {
        var resource = new Resource(cssResourcePath, resourceLoaderClass);
        EXTRA_STYLESHEETS.add(resource);
    }

    protected static Set<Resource> getExtraStylesheets() {
        return Collections.unmodifiableSet(EXTRA_STYLESHEETS);
    }
}
