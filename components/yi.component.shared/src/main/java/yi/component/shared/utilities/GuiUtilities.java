package yi.component.shared.utilities;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.KeyModifier;

import java.util.Arrays;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class GuiUtilities {

    private GuiUtilities() {

    }

    /**
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values
     * rather than 0-1.0.
     * <p>
     * Values exceeding these boundaries will be clipped.
     *
     * @param r255 Red value, 0 - 255 inclusive
     * @param g255 Green value, 0 - 255 inclusive
     * @param b255 Blue value, 0 - 255 inclusive
     * @return A color of the defined RGB value
     */
    public static Color getColor(int r255, int g255, int b255) {
        return getColor(r255, g255, b255, 1.0d);
    }

    /**
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values
     * rather than 0-1.0.
     * However, the alpha value is still between 0 - 1.0 inclusive.
     * <p>
     * Values exceeding these boundaries will be clipped.
     *
     * @param r255 Red value, 0 - 255 inclusive
     * @param g255 Green value, 0 - 255 inclusive
     * @param b255 Blue value, 0 - 255 inclusive
     * @return A color of the defined RGB value
     */
    public static Color getColor(int r255, int g255, int b255, double alpha1) {
        double r = trimToRange(r255 / 255d);
        double g = trimToRange(g255 / 255d);
        double b = trimToRange(b255 / 255d);
        alpha1 = trimToRange(alpha1);

        return new Color(r, g, b, alpha1);
    }

    private static double trimToRange(double value) {
        if (value < 0.0) {
            value = 0.0;
        }

        if (value > 1.0) {
            value = 1.0;
        }
        
        return value;
    }

    /**
     * Helper method to create Fx {@link KeyCombination} in a readable way.
     *
     * @param keyCode Main key combination keycode
     * @param modifiers Modifier states, the presence of which indicates the modifier key
     *                  must be held down. Otherwise the key must be up.
     * @return The requested key combination.
     *
     * @see KeyModifier Available modifier keys
     */
    public static KeyCombination getKeyCombination(KeyCode keyCode, KeyModifier ... modifiers) {
        Set<KeyModifier> modifierSet = Arrays.stream(modifiers).collect(Collectors.toSet());
        boolean shortcutDown = modifierSet.contains(KeyModifier.SHORTCUT);
        boolean ctrlDown = modifierSet.contains(KeyModifier.CTRL);
        boolean altDown = modifierSet.contains(KeyModifier.ALT);
        boolean shiftDown = modifierSet.contains(KeyModifier.SHIFT);
        boolean metaDown = modifierSet.contains(KeyModifier.META);

        return new KeyCodeCombination(keyCode,
                getModifierValue(shiftDown),
                getModifierValue(ctrlDown),
                getModifierValue(altDown),
                getModifierValue(metaDown),
                getModifierValue(shortcutDown));
    }

    private static KeyCombination.ModifierValue getModifierValue(boolean modifierDown) {
        return modifierDown ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP;
    }

    /**
     * Returns the calculated size for a body of text using the given font.
     *
     * @param font Font used to display the text.
     * @param text Text data to be displayed.
     * @return Calculated bounds for the text.
     */
    public static Bounds getTextBoundsLocal(@NotNull Font font, String text) {
        var metricsTest = new Text(text);
        metricsTest.setFont(font);
        return metricsTest.getBoundsInLocal();
    }

    /**
     * Creates an empty panel whose width is automatically calculated based on the
     * container width. This can be used to center elements like so:<br />
     * <pre>
     *     toolBar.getItems().add(createDynamicSpacer());
     *     // Add toolbar items
     *     toolBar.getItems().add(createDynamicSpacer());
     * </pre>
     * Alternatively, using just one dynamic spacer at either side will push all
     * items to the other side of the container.
     *
     * @return Spacer component to be added to a container.
     */
    public static Pane createDynamicSpacer() {
        var dynamicSpacer = new Pane();
        dynamicSpacer.setMinWidth(1);
        HBox.setHgrow(dynamicSpacer, Priority.SOMETIMES);

        return dynamicSpacer;
    }

    /**
     * Creates an empty panel of specified width to pad out components in a container.
     *
     * @param width Spacer width in pixels.
     * @return Spacer component to be added to a container.
     */
    public static Pane createStaticSpacer(int width) {
        var spacer = new Pane();
        spacer.setPrefWidth(width);

        return spacer;
    }

    public static void traverseRecursive(Parent nodeToTraverse, Consumer<Node> nodeConsumer) {
        var items = new Stack<Node>();
        items.push(nodeToTraverse);

        while (!items.isEmpty()) {
            var item = items.pop();
            nodeConsumer.accept(item);

            if (item instanceof Parent) {
                for (var child : ((Parent) item).getChildrenUnmodifiable()) {
                    items.push(child);
                }
            }
        }
    }

    public static void traverseMenuRecursive(MenuBar menuBar, Consumer<MenuItem> menuItemConsumer) {
        var itemsToVisit = new Stack<MenuItem>();
        itemsToVisit.addAll(menuBar.getMenus());

        while (!itemsToVisit.isEmpty()) {
            var menuItem = itemsToVisit.pop();
            menuItemConsumer.accept(menuItem);

            if (menuItem instanceof Menu) {
                itemsToVisit.addAll(((Menu) menuItem).getItems());
            }
        }
    }
}
