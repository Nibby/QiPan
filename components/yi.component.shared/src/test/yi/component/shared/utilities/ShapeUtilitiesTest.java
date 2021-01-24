package yi.component.shared.utilities;

import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ShapeUtilitiesTest {

    @Test
    public void testCenterFit_squareContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 100, 100), fit);
    }

    @Test
    public void testCenterFit_squareContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 25, 100, 50), fit);
    }

    @Test
    public void testCenterFit_squareContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 100);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(25, 0, 50, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(50, 0, 100, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 200, 100), fit);
    }

    @Test
    public void testCenterFit_wideContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 200, 100);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(75, 0, 50, 100), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_squareFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 1.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 50, 100, 100), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_wideFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 2.0d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 75, 100, 50), fit);
    }

    @Test
    public void testCenterFit_tallContainerNoInsets_tallFitRatio() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0d);

        testFitCorrect(new Rectangle(0, 0, 100, 200), fit);
    }

    @Test
    public void testCenterFit_tallContainerFivePercentInsets_tallFitCenteredCorrectly() {
        Rectangle container = new Rectangle(0, 0, 100, 200);
        double fitWidthToHeightRatio = 0.5d;

        Rectangle fit = ShapeUtilities.centerFit(container, fitWidthToHeightRatio, 0.05d);

        testFitCorrect(new Rectangle(5, 5, 90, 190), fit);
    }

    private void testFitCorrect(Rectangle expected, Rectangle actual) {
        System.out.println("Testing fitExpected=" + expected.toString() + ", actual=" + actual.toString());

        Assertions.assertTrue(ComparisonUtilities.doubleEquals(actual.getX(), expected.getX()));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(actual.getY(), expected.getY()));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(actual.getWidth(), expected.getWidth()));
        Assertions.assertTrue(ComparisonUtilities.doubleEquals(actual.getHeight(), expected.getHeight()));
    }

}
