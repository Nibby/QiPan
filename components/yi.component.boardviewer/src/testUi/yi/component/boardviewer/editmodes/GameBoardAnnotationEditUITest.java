package yi.component.boardviewer.editmodes;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import yi.component.boardviewer.GameBoardUITestBase;
import yi.core.go.*;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardAnnotationEditUITest extends GameBoardUITestBase {

    @Start
    public void start(Stage stage) {
        super.start(stage);
    }

    @AfterEach
    public void dispose() {
        super.dispose();
    }

    @Test
    public void testAddOne_PointAnnotation(FxRobot robot) {
        getBoard().setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = getBoardManager().size;
        double[] intersection = size.getGridRenderPosition(0, 0, 0);
        double x = intersection[0];
        double y = intersection[1];

        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(x, y), Motion.DEFAULT);
        robot.clickOn(MouseButton.PRIMARY);

        assertTrue(getGameModel().getAnnotationsOnCurrentNode().contains(new Annotation.Square(0, 0)),
                "No square annotation created after mouse click.");
    }

    @Test
    public void testAddMultiple_PointAnnotation(FxRobot robot) {
        getBoard().setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = getBoardManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(getBoardWidth() - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        for (int i = 0; i < getBoardWidth(); ++i) {
            assertTrue(getGameModel().getAnnotationsOnCurrentNode().contains(new Annotation.Square(i, 0)),
                    "No square annotation created at (" + i + ", 0) after mouse drag from (0, 0) to (" + (getBoardWidth()-1) + ", 0).");
        }
    }

    @Test
    public void testEraseMultiple_PointAnnotation(FxRobot robot) {
        getBoard().setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = getBoardManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(getBoardWidth() - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);
        robot.sleep(100);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        assertEquals(0, getGameModel().getAnnotationsOnCurrentNode().size(),
                "Annotations are not fully cleared.");
    }

    @Test
    public void testAddMultiple_PointAnnotation_ReplaceExistingAnnotations(FxRobot robot) {
        getBoard().setEditMode(EditMode.annotation(AnnotationType.SQUARE));

        var size = getBoardManager().size;
        double[] startPoint = size.getGridRenderPosition(0, 0, 0);
        double xStart = startPoint[0];
        double yStart = startPoint[1];

        double[] endPoint = size.getGridRenderPosition(getBoardWidth() - 1, 0, 0);
        double xEnd = endPoint[0];
        double yEnd = endPoint[1];

        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xEnd, yEnd), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        getBoard().setEditMode(EditMode.annotation(AnnotationType.CROSS));

        robot.drag(MouseButton.PRIMARY);
        robot.moveTo(getBoard().getComponent(), Pos.TOP_LEFT, new Point2D(xStart, yStart), Motion.DEFAULT);
        robot.release(MouseButton.PRIMARY);

        for (int i = 0; i < getBoardWidth(); ++i) {
            assertTrue(getGameModel().getAnnotationsOnCurrentNode().contains(new Annotation.Cross(i, 0)),
                    "No cross annotation at (" + i + ", 0) after mouse drag from (0, 0) to (" + (getBoardWidth()-1) + ", 0) to replace the row of squares.");
        }
    }

    @Test
    public void testAddLabel_Number_OneAtATime_TextIsDifferent(FxRobot robot) {
        testAddLabel_OneAtATime_TextIsDifferent(robot, AnnotationEditMode.LabelType.NUMBER);
    }

    @Test
    public void testAddLabel_Number_DragAcross_TextIsIdentical(FxRobot robot) {
        testAddLabel_DragAcross_TextIsIdentical(robot, AnnotationEditMode.LabelType.NUMBER);
    }

    @Test
    public void testAddLabel_Number_MixedEdits_WorksAsExpected(FxRobot robot) {
        testAddLabel_MixedEdits_WorksAsExpected(robot, AnnotationEditMode.LabelType.NUMBER);
    }

    @Test
    public void testAddLabel_Letter_OneAtATime_TextIsDifferent(FxRobot robot) {
        testAddLabel_OneAtATime_TextIsDifferent(robot, AnnotationEditMode.LabelType.LETTER);
    }

    @Test
    public void testAddLabel_Letter_DragAcross_TextIsIdentical(FxRobot robot) {
        testAddLabel_DragAcross_TextIsIdentical(robot, AnnotationEditMode.LabelType.LETTER);
    }

    @Test
    public void testAddLabel_Letter_MixedEdits_WorksAsExpected(FxRobot robot) {
        testAddLabel_MixedEdits_WorksAsExpected(robot, AnnotationEditMode.LabelType.LETTER);
    }

    @Test
    public void testAddLabel_OneAtATime_TextIsDifferent(FxRobot robot, AnnotationEditMode.LabelType labelType) {
        getBoard().setEditMode(EditMode.annotationLabel(AnnotationEditMode.LabelType.NUMBER));

        click(robot, 0, 0);
        click(robot, 1, 0);
        click(robot, 2, 0);

        var annotations = getGameModel().getAnnotationsOnCurrentNode();
        var annotationText = new HashSet<String>();

        for (Annotation annotation : annotations) {
            assertTrue(annotation instanceof Annotation.Label, "Set to edit " + labelType.name() + " label annotation but " +
                    "actual annotation is not instance of label.");

            var label = (Annotation.Label) annotation;
            String text = label.getText();
            assertFalse(annotationText.contains(text), "Another label already has the text \"" + text + "\".");

            annotationText.add(text);
        }
    }

    @Test
    public void testAddLabel_DragAcross_TextIsIdentical(FxRobot robot, AnnotationEditMode.LabelType labelType) {
        if (getBoardWidth() < 2) {
            throw new IllegalStateException("Cannot run this test because getBoard() width is too small.");
        }

        getBoard().setEditMode(EditMode.annotationLabel(AnnotationEditMode.LabelType.NUMBER));

        drag(robot, 0, 0, getBoardWidth(), 0);

        var annotations = getGameModel().getAnnotationsOnCurrentNode();

        String firstText = null;
        for (Annotation annotation : annotations) {
            assertTrue(annotation instanceof Annotation.Label, "Set to edit " + labelType.name() + " label annotation but " +
                    "actual annotation is not label.");

            var label = (Annotation.Label) annotation;
            String text = label.getText();
            if (firstText == null) {
                firstText = text;
            } else {
                assertEquals(firstText, text, "Label does not have identical text as first text when dragging to create annotation.");
            }
        }
    }

    @Test
    public void testAddLabel_MixedEdits_WorksAsExpected(FxRobot robot, AnnotationEditMode.LabelType labelType) {
        if (getBoardWidth() < 2) {
            throw new IllegalStateException("Cannot run this test because getBoard() width is too small.");
        }

        getBoard().setEditMode(EditMode.annotationLabel(labelType));

        drag(robot, 0, 0, getBoardWidth(), 0);

        var annotations = getGameModel().getAnnotationsOnCurrentNode();

        String firstText = null;
        for (Annotation annotation : annotations) {
            assertTrue(annotation instanceof Annotation.Label, "Set to edit " + labelType.name() + " label annotation but " +
                    "actual annotation is not label.");

            var label = (Annotation.Label) annotation;
            String text = label.getText();
            if (firstText == null) {
                firstText = text;
            } else {
                assertEquals(firstText, text, "Label does not have identical text as first text when dragging to create annotation.");
            }
        }

        click(robot, 0, 1);
        click(robot, 0, 2);
        click(robot, 0, 3);

        assertEquals(new Annotation.Label(0, 1, "2"), getAnnotationOrFail(0, 1), "Annotation mismatch - 1st click after dragging.");
        assertEquals(new Annotation.Label(0, 2, "3"), getAnnotationOrFail(0, 2), "Annotation mismatch - 2nd click after dragging.");
        assertEquals(new Annotation.Label(0, 3, "4"), getAnnotationOrFail(0, 3), "Annotation mismatch - 3rd click after dragging.");
    }

    @Override
    protected int getBoardWidth() {
        return 9;
    }

    @Override
    protected int getBoardHeight() {
        return 9;
    }

    @Override
    protected StandardGameRules getGameRules() {
        return StandardGameRules.CHINESE;
    }
}
