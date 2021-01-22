package yi.editor.framework.action;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import yi.editor.EditorUITestBase;
import yi.editor.components.EditorMainMenuType;
import yi.editor.components.EditorMenuBar;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.EditorAccelerator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests the sub-menu hierarchy is constructed correctly.
 */
public class EditorSubMenuActionTest extends EditorUITestBase {

    @Start
    @Override
    public void startFx(Stage stage) {
        super.startFx(stage);
    }

    private EditorSubMenuAction subMenu1;
    private EditorAction itemInSubMenu1;
    private final AtomicBoolean itemInSubMenu1ActionExecuted = new AtomicBoolean(false);

    private EditorSubMenuAction subMenu2;
    private EditorAction itemInSubMenu2;
    private final AtomicBoolean itemInSubMenu2ActionExecuted = new AtomicBoolean(false);

    @Override
    protected void initializeTestActions(EditorActionManager actionManager) {
        subMenu1 = new EditorSubMenuAction(EditorTextResources.MENUITEM_NEW_GAME);
        subMenu1.setInMenuBar(EditorMainMenuType.TESTING, 0d);

        itemInSubMenu1 = new EditorBasicAction(EditorTextResources.MENU_PERSPECTIVE,
                context -> itemInSubMenu1ActionExecuted.set(true));
        itemInSubMenu1.setInMenuBar(EditorMainMenuType.TESTING, 0d);
        itemInSubMenu1.setAccelerator(EditorAccelerator.TEST_ACCEL_1);

        subMenu1.addChildAction(itemInSubMenu1);

        subMenu2 = new EditorSubMenuAction(EditorTextResources.MENUITEM_SAVE_AS_GAME);
        subMenu2.setInMenuBar(EditorMainMenuType.TESTING, 1d);

        itemInSubMenu2 = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_GAME,
                context -> itemInSubMenu2ActionExecuted.set(true));
        itemInSubMenu2.setInMenuBar(EditorMainMenuType.TESTING, 1d);
        itemInSubMenu2.setAccelerator(EditorAccelerator.TEST_ACCEL_2);

        subMenu2.addChildAction(itemInSubMenu2);
        subMenu1.addChildAction(subMenu2);

        /*
            We should have a hierarchy that looks like:

            subMenu1 >  itemInSubMenu1
                            subMenu2   >  itemInSubMenu2
         */

        actionManager.addActions(new EditorAction[] {
                subMenu1,
                subMenu2,
                itemInSubMenu1,
                itemInSubMenu2
        });
    }

    @Test
    public void testSubMenuActionsCorrectness(FxRobot robot) throws InterruptedException {
        testStructureCorrectness();
        testActionsExecutable(robot);
        testAllActionsInMenuBarAreRoughlyCorrect();
    }

    private void testAllActionsInMenuBarAreRoughlyCorrect() {
        EditorMenuBar menuBar = window.getMainMenuBar();
        for (Menu menu : menuBar.getMenus()) {
            testMenuCorrectnessRecursive(menu, 0);
        }
    }

    private void testMenuCorrectnessRecursive(Menu menu, int depth) {
        for (MenuItem item : menu.getItems()) {
            testMenuItem(menu, depth);
            if (item instanceof Menu) {
                testMenuCorrectnessRecursive((Menu) item, depth + 1);
            }
        }

    }

    private void testMenuItem(MenuItem menuItem, int depth) {
        if (depth == 0) {
            Assertions.assertTrue(menuItem.getUserData() instanceof EditorMainMenuType,
                    menuItem.getClass().getSimpleName() + " '" + menuItem.getText()
                            + "' user data is not an instance of "
                            + EditorMainMenuType.class.getSimpleName() + ". Instead it is: "
                            + menuItem.getUserData().getClass().getName());
        } else {
            Assertions.assertTrue(menuItem.getUserData() instanceof EditorAction,
                    menuItem.getClass().getSimpleName() + " '" + menuItem.getText()
                            + "' user data is not an instance of "
                            + EditorAction.class.getSimpleName() + ". Instead it is: "
                            + menuItem.getUserData().getClass().getName());

            EditorAction action = (EditorAction) menuItem.getUserData();
            Assertions.assertTrue(action.isAddedToMenu(), action + " is not marked as " +
                    "added to menu even though it is on the menu hierarchy.");
            if (depth == 1) {
                Assertions.assertTrue(action.isTopLevelMenuItem(), action + " is a top level " +
                        "menu action but isTopLevelMenuItem() returned false.");
            } else {
                Assertions.assertFalse(action.isTopLevelMenuItem(), action + " is not a top " +
                        "level menu action but isTopLevelMenuItem() returned true.");
            }
        }
    }

    private void testActionsExecutable(FxRobot robot) throws InterruptedException {
        robot.clickOn(window.getStage());
        Thread.sleep(100);

        robot.press(KeyCode.CONTROL, KeyCode.ALT, KeyCode.SHIFT, KeyCode.DIGIT1);
        Thread.sleep(100);
        robot.release(KeyCode.CONTROL, KeyCode.ALT, KeyCode.SHIFT, KeyCode.DIGIT1);
        Thread.sleep(100);

        Assertions.assertTrue(itemInSubMenu1ActionExecuted.get(),
                "Pressing the shortcut keys for itemInSubMenu1 did not cause it to its action");
        Assertions.assertFalse(itemInSubMenu2ActionExecuted.get(),
                "Pressing the shortcut keys for itemInSubMenu1 somehow caused itemInSubMenu2 to run its action");

        robot.press(KeyCode.CONTROL, KeyCode.ALT, KeyCode.SHIFT, KeyCode.DIGIT2);
        Thread.sleep(100);
        robot.release(KeyCode.CONTROL, KeyCode.ALT, KeyCode.SHIFT, KeyCode.DIGIT2);

        Assertions.assertTrue(itemInSubMenu2ActionExecuted.get(),
                "Pressing the shortcut keys for itemInSubMenu2 didn't run its action");
    }

    private void testStructureCorrectness() {
        Menu testingMenu = getMenu(EditorMainMenuType.TESTING).orElseThrow();

        ObservableList<MenuItem> layer1Items = testingMenu.getItems();
        Assertions.assertEquals(1, layer1Items.size(),
                "Only subMenu1 should appear in first layer, got " + layer1Items.size() + " items");
        Assertions.assertEquals(subMenu1.getAsMenuItem(), layer1Items.get(0),
                "The only item in first layer is not subMenu1's component " +
                        "(or at least .equals() returned false)");
        Assertions.assertEquals(subMenu1, subMenu1.getAsMenuItem().getUserData(),
                "subMenu1 menu item user data does not point to itself.");

        MenuItem theOnlyLayer1Item = layer1Items.get(0);
        Assertions.assertTrue(theOnlyLayer1Item instanceof Menu,
                "subMenu1 exported a menu component that is not of type Menu");

        ObservableList<MenuItem> layer2Items = ((Menu) theOnlyLayer1Item).getItems();
        Assertions.assertEquals(2, layer2Items.size(),
                "itemInSubMenu1 and subMenu2 appears in layer 2, in that order. " +
                        "Actual number of menu items in this layer is " + layer2Items.size());
        Assertions.assertEquals(itemInSubMenu1.getAsMenuItem(), layer2Items.get(0),
                "First item in layer2 is not itemInSubMenu1 (is it ordered correctly?)");
        Assertions.assertEquals(itemInSubMenu1, itemInSubMenu1.getAsMenuItem().getUserData(),
                "itemInSubMenu1 menu item user data does not point to itself.");
        Assertions.assertEquals(subMenu2.getAsMenuItem(), layer2Items.get(1),
                "Second item in layer2 is not subMenu2  (is it ordered correctly?)");
        Assertions.assertEquals(subMenu2, subMenu2.getAsMenuItem().getUserData(),
                "subMenu2 menu item user data does not point to itself.");

        MenuItem layer2SubMenu = layer2Items.get(1);
        Assertions.assertTrue(layer2SubMenu instanceof Menu,
                "Second item in layer 2 is not an instance of Menu");

        ObservableList<MenuItem> layer3Items = ((Menu) layer2SubMenu).getItems();
        Assertions.assertEquals(1, layer3Items.size(),
                "Only itemInSubMenu2 should appear in layer 3, got " + layer3Items.size()
                        + " components");
        Assertions.assertEquals(itemInSubMenu2.getAsMenuItem(), layer3Items.get(0),
                "Item in layer 3 is not the same item exported by itemInSubMenu2, " +
                        "or at least equals() returned false.");
        Assertions.assertEquals(itemInSubMenu2, layer3Items.get(0).getUserData(),
                "itemsInSubMenu2 menu item user data does not point to itself.");
    }
}
