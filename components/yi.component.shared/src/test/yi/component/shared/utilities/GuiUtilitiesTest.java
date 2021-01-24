package yi.component.shared.utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class GuiUtilitiesTest {

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    public void testGetColor_clipsValueRange_conversionIsCorrect() {
        for (int i = -10; i < 265; ++i) {
            int r = i;
            int g = i;
            int b = i;
            double expectA = i / 255d;
            var color = GuiUtilities.getColor(r, g, b, expectA);

            double expectR;
            double expectG;
            double expectB;

            if (i < 0) {
                expectA = expectB = expectG = expectR = 0d;
            } else if (i > 255) {
                expectA = expectB = expectG = expectR = 1d;
            } else {
                expectR = i / 255d;
                expectG = i / 255d;
                expectB = i / 255d;
            }

            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectR, color.getRed()), "Red value difference > tolerance. Expected: " + expectR + ", actual: " + r);
            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectG, color.getGreen()), "Green value difference > tolerance. Expected: " + expectG + ", actual: " + g);
            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectB, color.getBlue()), "Blue value difference > tolerance. Expected: " + expectB + ", actual: " + b);
            Assertions.assertTrue(ComparisonUtilities.doubleEquals(expectA, color.getOpacity()), "Alpha value difference > tolerance. Expected: " + expectA + ", actual: " + color.getOpacity());
        }
    }

}
