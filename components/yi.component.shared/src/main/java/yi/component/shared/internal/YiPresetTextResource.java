package yi.component.shared.internal;

import yi.component.shared.i18n.I18n;
import yi.component.shared.i18n.TextResource;

import java.util.ResourceBundle;

/**
 * A collection of default component text.
 */
public final class YiPresetTextResource {

    private YiPresetTextResource() {

    }

    private static final String I18N_PACKAGE = "yi.component.shared.i18n.";
    private static final String BUNDLE_NAME = I18N_PACKAGE + "Presets";
    private static final ResourceBundle BUNDLE = I18n.getResourceBundle(BUNDLE_NAME,
            YiPresetTextResource.class.getModule());

    public static final TextResource EMPTY = resource("empty");
    public static final TextResource BUTTON_OKAY = resource("okay");
    public static final TextResource BUTTON_CANCEL = resource("cancel");
    public static final TextResource BUTTON_APPLY = resource("apply");

    private static TextResource resource(String i18nKey) {
        return new TextResource(i18nKey, BUNDLE);
    }
}
