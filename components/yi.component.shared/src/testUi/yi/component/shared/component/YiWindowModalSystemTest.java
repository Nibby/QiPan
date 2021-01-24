package yi.component.shared.component;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.component.shared.UITestHelper;
import yi.component.shared.component.modal.ModalActionButton;
import yi.component.shared.component.modal.YiModalContent;

import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(ApplicationExtension.class)
public final class YiWindowModalSystemTest {

    private YiWindow window;

    @SuppressWarnings("unused")
    @Start
    public void start(Stage stage) {
        window = new YiWindow();
        stage.setScene(window.getScene().getScene());
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        UITestHelper.onFxThread(isDone -> {
            window.getStage().close();
            isDone.set(true);
        });
    }

    @Test
    public void testDimWorks() throws InterruptedException {
        testDim(true);
    }

    @Test
    public void testNoDim_Works() throws InterruptedException {
        testDim(false);
    }

    private void testDim(boolean isDimmed) throws InterruptedException {
        UITestHelper.onFxThread(isDone -> {
            var modalContent = new TestModalContent();
            modalContent.setContentDimmed(isDimmed);
            window.pushModalContent(modalContent,
                () -> {
                    var dimState = window.getScene().getGlassPane().isDimmed();
                    if (isDimmed) {
                        Assertions.assertTrue(dimState);
                    } else {
                        Assertions.assertFalse(dimState);
                    }
                    isDone.set(true);
                });
        });
    }

    @Test
    public void testNoStrict_pressEscExits(FxRobot robot) throws InterruptedException {
        AtomicLong escPressTime = new AtomicLong(0);
        AtomicBoolean escPressed = new AtomicBoolean(false);
        var modalRef = new AtomicReference<TestModalContent>();

        UITestHelper.onFxThread(isDone -> {
            var modalContent = new TestModalContent();
            modalRef.set(modalContent);
            modalContent.setStrictModal(false);

            window.pushModalContent(modalContent, () -> {
                robot.clickOn(window.getScene().getScene());
                robot.press(KeyCode.ESCAPE);
                escPressed.set(true);
                escPressTime.set(System.nanoTime());

                isDone.set(true);
            });

        });

        while (!escPressed.get() || TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - escPressTime.get()) < 1000) {
            Thread.sleep(500);
        }

        Assertions.assertTrue(modalRef.get().closeCalled);
        Assertions.assertTrue(window.getScene().getModalStack().isEmpty());
        Assertions.assertFalse(window.getScene().isModalMode());
    }

    @Test
    public void testNoStrict_clickInBackgroundAreaExits(FxRobot robot) throws InterruptedException {
        testExit(false, false, robot, true);
    }

    @Test
    public void testNoStrict_clickInDimAreaExits(FxRobot robot) throws InterruptedException {
        testExit(true, false, robot, true);
    }

    @Test
    public void testStrict_clickInBackgroundArea_doesNotExit(FxRobot robot) throws InterruptedException {
        testExit(false, true, robot, false);
    }

    @Test
    public void testStrict_clickInDimArea_doesNotExit(FxRobot robot) throws InterruptedException {
        testExit(true, true, robot, false);
    }

    private void testExit(boolean dim, boolean strict, FxRobot robot, boolean expectExit) throws InterruptedException {
        // This test relies on the size of the modal content being smaller than the
        // stage.
        UITestHelper.onFxThread(isDone -> {
            var modalContent = new TestModalContent();
            modalContent.setStrictModal(strict);
            modalContent.setContentDimmed(dim);

            window.pushModalContent(modalContent, () -> {
                var glassPane = window.getScene().getGlassPane();
                var localBounds = glassPane.getBoundsInLocal();
                var boundsOnScreen = glassPane.localToScreen(localBounds);

                robot.moveTo(boundsOnScreen.getMinX() + 10, boundsOnScreen.getMinY() + 10);
                robot.clickOn(MouseButton.PRIMARY);

                isDone.set(true);
            });

        });
        Thread.sleep(500); // Wait some time for animation to finish.
        var modalState = window.getScene().isModalMode();
        if (expectExit) {
            Assertions.assertFalse(modalState);
        } else {
            Assertions.assertTrue(modalState);
        }

    }

    @Test
    public void testModalContentVisible_cannotUseMenuItems(FxRobot robot) throws InterruptedException {
        var triggeredAction = new AtomicBoolean(false);

        UITestHelper.onFxThread(isDone -> {
            MenuItem testMenuItem = new MenuItem("Test item");
            testMenuItem.setOnAction(actionEvent -> triggeredAction.set(true));
            testMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.DOWN)
            );
            Menu testMenu = new Menu("Test");
            testMenu.getItems().add(testMenuItem);
            MenuBar mainMenu = new MenuBar();
            mainMenu.getMenus().add(testMenu);
            window.setMainMenuBar(mainMenu);

            var modalContent = new TestModalContent();
            modalContent.setStrictModal(false);
            modalContent.setContentDimmed(true);

            window.pushModalContent(modalContent, () -> {
                robot.press(KeyCode.SHORTCUT);
                robot.press(KeyCode.T);
                isDone.set(true);
            });

        });
        Thread.sleep(500);
        Assertions.assertFalse(triggeredAction.get(), "Menu item action can still be " +
                "triggered after modal content is pushed and visible.");
    }

    @Test
    public void testModalContentDismissed_canUseMenuItemsAgain(FxRobot robot)
            throws InterruptedException {
        var triggeredAction = new AtomicBoolean(false);

        UITestHelper.onFxThread(isDone -> {
            MenuItem testMenuItem = new MenuItem("Test item");
            testMenuItem.setOnAction(actionEvent -> triggeredAction.set(true));
            testMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.DOWN)
            );
            Menu testMenu = new Menu("Test");
            testMenu.getItems().add(testMenuItem);
            MenuBar mainMenu = new MenuBar();
            mainMenu.getMenus().add(testMenu);
            window.setMainMenuBar(mainMenu);

            var modalContent = new TestModalContent();
            modalContent.setStrictModal(false);
            modalContent.setContentDimmed(true);

            window.pushModalContent(modalContent,
                () -> {
                    window.getScene().removeModalContent(modalContent, () -> {
                        isDone.set(true);
                    });
                });

        });
        Thread.sleep(100);
        robot.press(KeyCode.SHORTCUT);
        robot.press(KeyCode.T);
        Thread.sleep(100);
        Assertions.assertTrue(triggeredAction.get(),
                "Menu item did not respond to its accelerator after modal content was " +
                        "pushed then dismissed.");
    }

    @Test
    public void testModalContentVisible_cannotFocusMainContent(FxRobot robot) throws InterruptedException {
        var contentRoot = new AtomicReference<BorderPane>();
        var textArea = new AtomicReference<TextArea>();
        UITestHelper.onFxThread(isDone -> {
            contentRoot.set(new BorderPane());
            textArea.set(new TextArea("Lorem ipsum"));
            textArea.get().requestFocus();
            isDone.set(true);
        });

        Thread.sleep(100);
        robot.type(KeyCode.SPACE);
        robot.type(KeyCode.H);
        robot.type(KeyCode.I);
        Thread.sleep(100);

        UITestHelper.onFxThread(isDone -> {
            var modalContent = new TestModalContent();
            modalContent.setStrictModal(false);
            modalContent.setContentDimmed(true);
            window.pushModalContent(modalContent, () -> {
                Assertions.assertFalse(textArea.get().isFocused());
                for (int i = 0; i < 10; ++i) {
                    robot.press(KeyCode.TAB);
                    robot.release(KeyCode.TAB);

                    Assertions.assertFalse(contentRoot.get().isFocused());
                    Assertions.assertFalse(textArea.get().isFocused());
                }
                isDone.set(true);
            });
        });
    }

    @Test
    public void testMultipleModalContent_oneIsThenDismissed_showsPreviousModalContent() throws InterruptedException {
        UITestHelper.onFxThread(isDone -> {
            var firstModal = new TestModalContent();
            firstModal.setStrictModal(false);
            firstModal.setContentDimmed(true);
            window.pushModalContent(firstModal, () -> {
                var secondModal = new TestModalContent();
                secondModal.setStrictModal(false);
                secondModal.setContentDimmed(true);
                window.pushModalContent(secondModal, () -> {
                    final var modalStack = window.getScene().getModalStack();
                    Assertions.assertEquals(2, modalStack.size());
                    Assertions.assertEquals(secondModal, modalStack.peek());
                    window.getScene().removeModalContent(secondModal, () -> {
                        Assertions.assertEquals(1, modalStack.size());
                        Assertions.assertEquals(firstModal, modalStack.peek());
                        isDone.set(true);
                    });
                });
            });
        });
    }

    @Test
    public void testCannotTriggerModalContentDuringAnimation(FxRobot robot) throws InterruptedException {
        var addedModalContents = new Stack<TestModalContent>();
        var firstModalContent = new AtomicReference<TestModalContent>(null);

        UITestHelper.onFxThread(isDone -> {
            MenuItem testMenuItem = new MenuItem("Test item");
            testMenuItem.setOnAction(actionEvent -> {
                var modal = new TestModalContent();
                modal.setStrictModal(true);
                modal.setContentDimmed(true);
                window.pushModalContent(modal, () -> {
                    addedModalContents.push(modal);
                    synchronized (firstModalContent) {
                        if (firstModalContent.get() == null) {
                            firstModalContent.set(modal);
                        }
                    }
                });
            });
            testMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP,
                    KeyCombination.ModifierValue.UP)
            );
            Menu testMenu = new Menu("Test");
            testMenu.getItems().add(testMenuItem);
            MenuBar mainMenu = new MenuBar();
            mainMenu.getMenus().add(testMenu);
            window.setMainMenuBar(mainMenu);

            robot.clickOn(window.getScene().getScene());
            robot.press(KeyCode.T);
            robot.press(KeyCode.T);
            robot.press(KeyCode.T);

            isDone.set(true);
        });

        Thread.sleep(1000);

        Assertions.assertNotNull(firstModalContent.get());
        Assertions.assertEquals(firstModalContent.get(), addedModalContents.peek());
        Assertions.assertEquals(1, addedModalContents.size());
    }

    @Test
    public void testEnterKey_triggersDefaultActionExecution(FxRobot robot) throws InterruptedException {
        var modalReference = new AtomicReference<TestModalContent>();

        UITestHelper.onFxThread(isDone -> {
            var modal = new TestModalContent();
            modalReference.set(modal);
            modal.setStrictModal(false);
            modal.setContentDimmed(true);
            window.pushModalContent(modal, () -> {
                robot.type(KeyCode.ENTER);
                isDone.set(true);
            });
        });
        Thread.sleep(100);
        Assertions.assertNotNull(modalReference.get());
        Assertions.assertTrue(modalReference.get().defaultActionExecuted);
    }

    private static final class TestModalContent implements YiModalContent {

        private boolean strictModal = false;
        private boolean contentDimmed = false;
        private YiScene scene = null;

        private boolean showCalled = false;
        private boolean closeCalled = false;
        private boolean defaultActionExecuted = false;

        @Override
        public @NotNull Parent getContent() {
            var content = new BorderPane();
            content.setTop(new Label("Test Label"));
            content.setMaxSize(400, 300);
            content.setBackground(
                new Background(
                    new BackgroundFill(
                        new Color(0.7d, 0.7d, 0.7d, 1.0d),
                        new CornerRadii(0d),
                        new Insets(0d)
                    )
                )
            );
            return content;
        }

        @Override
        public boolean isStrictModal() {
            return strictModal;
        }

        @Override
        public boolean isContentDimmed() {
            return contentDimmed;
        }

        @Override
        public void show(@NotNull YiScene scene) {
            showCalled = true;
            this.scene = scene;
        }

        @Override
        public void close(@Nullable ModalActionButton selectedActionButton) {
            assert scene != null;

            closeCalled = true;
            scene.removeModalContent(this);
        }

        @Override
        public void executeDefaultAction() {
            defaultActionExecuted = true;
        }

        public void setStrictModal(boolean strictModal) {
            this.strictModal = strictModal;
        }

        public void setContentDimmed(boolean contentDimmed) {
            this.contentDimmed = contentDimmed;
        }
    }

}
