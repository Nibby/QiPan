package yi.editor.framework;

import yi.component.shared.i18n.I18n;
import yi.component.shared.i18n.Language;
import yi.component.shared.i18n.TextResource;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Stores all the i18n translation keys used by the Yi editor module.
 */
public final class EditorTextResources {

    private static final String I18N_PACKAGE = "yi.editor.i18n.";

    private static final String BUNDLE_NAME = I18N_PACKAGE + "EditorTranslations";
    private static final ResourceBundle BUNDLE = I18n.getResourceBundle(BUNDLE_NAME, EditorTextResources.class.getModule());

    public static final TextResource EMPTY = standardResource("empty");

    public static final TextResource MENU_FILE = standardResource("menu.file");
    public static final TextResource MENU_EDIT = standardResource("menu.edit");
    public static final TextResource MENU_PERSPECTIVE = standardResource("menu.perspective");

    public static final TextResource MENU_TOOLS = standardResource("menu.tools");
    public static final TextResource MENU_NAVIGATE = standardResource("menu.navigate");
    public static final TextResource MENU_VIEW = standardResource("menu.view");
    public static final TextResource MENU_WINDOW = standardResource("menu.window");
    public static final TextResource MENU_HELP = standardResource("menu.help");
    public static final TextResource MENU_DEBUG = standardResource("menu.debug");
    public static final TextResource MENU_TESTING = standardResource("menu.testing");

    public static final TextResource MENUITEM_PERSPECTIVE_COMPACT = standardResource("menuItem.perspective.compact");
    public static final TextResource MENUITEM_PERSPECTIVE_REVIEW = standardResource("menuItem.perspective.review");

    public static final TextResource MENUITEM_NEW_GAME = standardResource("menuItem.newGame");
    public static final TextResource MENUITEM_NEW_WINDOW = standardResource("menuItem.newWindow");
    public static final TextResource MENUITEM_OPEN_GAME = standardResource("menuItem.openGame");
    public static final TextResource MENUITEM_SAVE_GAME = standardResource("menuItem.saveGame");
    public static final TextResource MENUITEM_SAVE_AS_GAME = standardResource("menuItem.saveAsGame");
    public static final TextResource MENUITEM_TOGGLE_COORDINATES = standardResource("menuItem.toggleCoordinates");

    public static final TextResource UNDO = standardResource("undo");
    public static final TextResource REDO = standardResource("redo");

    public static final TextResource TOOL_PLAY_MOVE = standardResource("tool.playMove");
    public static final TextResource TOOL_ADD_BLACK = standardResource("tool.addBlack");
    public static final TextResource TOOL_ADD_WHITE = standardResource("tool.addWhite");
    public static final TextResource TOOL_TRIANGLE = standardResource("tool.triangle");
    public static final TextResource TOOL_CIRCLE = standardResource("tool.circle");
    public static final TextResource TOOL_SQUARE = standardResource("tool.square");
    public static final TextResource TOOL_CROSS = standardResource("tool.cross");
    public static final TextResource TOOL_LABEL_LETTER = standardResource("tool.labelLetter");
    public static final TextResource TOOL_LABEL_NUMBER = standardResource("tool.labelNumber");
    public static final TextResource TOOL_LINE = standardResource("tool.line");
    public static final TextResource TOOL_ARROW = standardResource("tool.arrow");
    public static final TextResource TOOL_DIM = standardResource("tool.dim");

    public static final TextResource TOGGLE_PERSPECTIVE_REVIEW = standardResource("shortcut.togglePerspectiveReview");
    public static final TextResource TOGGLE_PERSPECTIVE_COMPACT = standardResource("shortcut.togglePerspectiveCompact");

    public static final TextResource DEFAULT_BLACK_NAME = standardResource("default.blackName");
    public static final TextResource DEFAULT_WHITE_NAME = standardResource("default.whiteName");

    public static final TextResource MOVE_COUNT = standardResource("moveCount");
    public static final TextResource PREVIEW_MOVE_PROMPT = standardResource("previewMoveText");

    public static final TextResource PASS = standardResource("pass");
    public static final TextResource REMOVE_NODE = standardResource("removeNode");

    public static final TextResource TO_PREVIOUS_NODE = standardResource("toPreviousNode");
    public static final TextResource TO_PREVIOUS_10_NODES = standardResource("toPrevious10Nodes");
    public static final TextResource TO_ROOT_NODE = standardResource("toRootNode");
    public static final TextResource TO_NEXT_NODE = standardResource("toNextNode");
    public static final TextResource TO_NEXT_10_NODES = standardResource("toNext10Nodes");
    public static final TextResource TO_VARIATION_END = standardResource("toVariationEnd");

    public static void installSupportedLanguages() {
        Language.add(new Language("\u7b80\u4f53\u4e2d\u6587", Locale.SIMPLIFIED_CHINESE));
    }

    private static TextResource standardResource(String i18nKey) {
        return new TextResource(i18nKey, BUNDLE);
    }
}
