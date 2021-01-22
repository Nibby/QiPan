package yi.editor.hack;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import yi.component.shared.component.YiScene;
import yi.editor.framework.action.EditorAction;

import java.util.Arrays;
import java.util.Set;

/**
 * Collection of dodgy code that works around some Java platform issues. Ideally we should
 * not rely on these and use a supported approach instead. For ease of maintenance, we dump
 * them here.
 */
public final class Hacks {

    private Hacks() {

    }

    /**
     *  On macOS, some menu item shortcuts don't work when the key combination does not
     *  contain at least one modifier. To support those accelerators, we manually register
     *  them to the scene here.
     */
    public static void fixSingleKeyAcceleratorsForMac(Set<EditorAction> actionSet, YiScene scene) {
        for (EditorAction action : actionSet) {
            action.getAccelerator().ifPresent(accelerator -> {
                KeyCombination keyCombination = accelerator.getKeyCombination();
                KeyCombination.ModifierValue[] modifiers = new KeyCombination.ModifierValue[] {
                        keyCombination.getAlt(),
                        keyCombination.getControl(),
                        keyCombination.getMeta(),
                        keyCombination.getShift(),
                        keyCombination.getShortcut(),
                };
                if (Arrays.stream(modifiers).noneMatch(modifier -> modifier == KeyCombination.ModifierValue.DOWN)) {
                    if (keyCombination.getName().contains(KeyCode.BACK_SPACE.getName())) {
                        scene.getAccelerators().put(keyCombination, action::performAction);
                    }
                }
            });
        }
    }
}
