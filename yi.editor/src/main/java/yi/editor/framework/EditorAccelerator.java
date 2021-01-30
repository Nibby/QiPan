package yi.editor.framework;

import javafx.scene.input.KeyCode;
import yi.component.shared.component.Accelerator;
import yi.component.shared.component.KeyModifier;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.SystemUtilities;

import java.util.function.Function;

/**
 * List of all the shortcut-keys that can be triggered in the application. Each shortcut
 * is mapped to an {@link Accelerator} instance which is created within the implementation
 * of this class.
 * <p/>
 * To assign a shortcut key to supported Fx components, use the following:
 * <pre>
 *     var accelerator = EditorAccelerator.SOME_ACCELERATOR.getAccelerator();
 *     accelerator.install(menuItem); // Or one of the other .install variants()
 * </pre>
 * or one of its overloaded variants.
 */
public enum EditorAccelerator {

    UNDO("undo", EditorTextResources.UNDO, KeyCode.Z, new KeyModifier[] {KeyModifier.SHORTCUT}),
    REDO("redo", (id) -> {
        Accelerator accelerator;
        if (SystemUtilities.isMac()) {
            accelerator = new Accelerator(id, EditorTextResources.REDO, KeyCode.Z, new KeyModifier[]{KeyModifier.SHORTCUT, KeyModifier.SHIFT});
        } else {
            accelerator = new Accelerator(id, EditorTextResources.REDO, KeyCode.Y, new KeyModifier[]{KeyModifier.SHORTCUT});
        }
        return accelerator;
    }),

    TOGGLE_PERSPECTIVE_COMPACT("togglePerspectiveCompact", EditorTextResources.TOGGLE_PERSPECTIVE_COMPACT, KeyCode.W, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOGGLE_PERSPECTIVE_REVIEW("togglePerspectiveReview", EditorTextResources.TOGGLE_PERSPECTIVE_REVIEW, KeyCode.E, new KeyModifier[] { KeyModifier.SHORTCUT }),

    TOGGLE_BOARD_COORDINATES("toggleBoardCoordinates", EditorTextResources.MENUITEM_TOGGLE_COORDINATES, KeyCode.C, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),

    TOOL_PLAY_MOVE("toolPlayMove", EditorTextResources.TOOL_PLAY_MOVE, KeyCode.DIGIT1, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_TRIANGLE("toolTriangle", EditorTextResources.TOOL_TRIANGLE, KeyCode.DIGIT3, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_SQUARE("toolSquare", EditorTextResources.TOOL_SQUARE, KeyCode.DIGIT4, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_CIRCLE("toolCircle", EditorTextResources.TOOL_CIRCLE, KeyCode.DIGIT5, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_CROSS("toolCross", EditorTextResources.TOOL_CROSS, KeyCode.DIGIT2, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LABEL_LETTER("toolLetter", EditorTextResources.TOOL_LABEL_LETTER, KeyCode.DIGIT6, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LABEL_NUMBER("toolNumber", EditorTextResources.TOOL_LABEL_NUMBER, KeyCode.DIGIT7, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_LINE("toolLine", EditorTextResources.TOOL_LINE, KeyCode.DIGIT8, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TOOL_ARROW("toolArrow", EditorTextResources.TOOL_ARROW, KeyCode.DIGIT9, new KeyModifier[] { KeyModifier.SHORTCUT }),

    NEW_GAME("newGame", EditorTextResources.MENUITEM_NEW_GAME, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT }),
    NEW_WINDOW("newWindow", EditorTextResources.MENUITEM_NEW_WINDOW, KeyCode.N, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),
    OPEN_GAME("openGame", EditorTextResources.MENUITEM_OPEN_GAME, KeyCode.O, new KeyModifier[] { KeyModifier.SHORTCUT }),
    SAVE_GAME("saveGame", EditorTextResources.MENUITEM_SAVE_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT }),
    SAVE_AS_GAME("saveAsGame", EditorTextResources.MENUITEM_SAVE_AS_GAME, KeyCode.S, new KeyModifier[] { KeyModifier.SHORTCUT, KeyModifier.SHIFT }),

    PASS("pass", EditorTextResources.PASS, KeyCode.P, new KeyModifier[] { KeyModifier.SHORTCUT }),
    REMOVE_NODE("removeNode", EditorTextResources.REMOVE_NODE, KeyCode.BACK_SPACE, new KeyModifier[0]),

    TO_PREVIOUS_NODE("toPreviousNode", EditorTextResources.SAVE, KeyCode.UP, new KeyModifier[0]),
    TO_PREVIOUS_10_NODES("toPrevious10Nodes", EditorTextResources.TO_PREVIOUS_10_NODES, KeyCode.UP, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TO_ROOT_NODE("toRootNode", EditorTextResources.TO_ROOT_NODE, KeyCode.UP, new KeyModifier[] { KeyModifier.SHIFT, KeyModifier.SHORTCUT }),
    TO_NEXT_NODE("toNextNode", EditorTextResources.TO_NEXT_NODE, KeyCode.DOWN, new KeyModifier[0]),
    TO_NEXT_10_NODES("toNext10Nodes", EditorTextResources.TO_NEXT_10_NODES, KeyCode.DOWN, new KeyModifier[] { KeyModifier.SHORTCUT }),
    TO_VARIATION_END("toVariationEnd", EditorTextResources.TO_VARIATION_END, KeyCode.DOWN, new KeyModifier[] { KeyModifier.SHIFT, KeyModifier.SHORTCUT }),

    EDIT_GAME_INFO("editGameInfo", EditorTextResources.EDIT_GAME_INFO, KeyCode.I, new KeyModifier[] { KeyModifier.SHORTCUT }),

    TEST_ACCEL_1("testAccel1", EditorTextResources.EMPTY, KeyCode.DIGIT1, new KeyModifier[] { KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT }),
    TEST_ACCEL_2("testAccel2", EditorTextResources.EMPTY, KeyCode.DIGIT2, new KeyModifier[] { KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT }),

    ;

    private final String id;
    private final Accelerator accelerator;

    EditorAccelerator(String id, Function<String, Accelerator> acceleratorSupplier) {
        this.id = id;
        this.accelerator = acceleratorSupplier.apply(id);
    }

    EditorAccelerator(String id,
                      TextResource name,
                      KeyCode keyCode,
                      KeyModifier[] modifiers) {
        this(id, (proxyId) -> new Accelerator(id, name, keyCode, modifiers));
    }

    public String getId() {
        return id;
    }

    public Accelerator getAccelerator() {
        return accelerator;
    }

    public static Accelerator getAccelerator(EditorAccelerator id) {
        var idAsString = id.getId();
        return Accelerator.getAccelerator(idAsString).orElseThrow();
    }
}
