package yi.component.boardviewer;

import javafx.scene.shape.Rectangle;
import yi.component.shared.utilities.ShapeUtilities;

import java.util.Optional;

/**
 * Manages the size parameters for drawing the game board. Properties in this class are
 * usually dynamically calculated depending on the dimension of the game board canvas.
 * For that reason, many properties use a hard-coded percentage rather than some constant
 * value.
 *
 * The percentage is usually taken from the shorter side of a boundary
 * (i.e. {@code Math.min(a, b) * percentage}.
 */
public final class GameBoardSize {

    private Rectangle stageBounds;
    private LayoutRectangle boardBounds;

    private LayoutRectangle coordinateLabelBounds;
    private Rectangle gridBounds;

    // These are percentages of gridBounds.
    private double stoneGapSize;
    private double stoneSize;
    private double gridUnitSize; // Size of the stone + gap at each intersection

    private double stoneShadowRadius;
    private double stoneShadowOffset;

    // Cached compute parameter values for recomputing
    private boolean cachedOnce = false;
    private double cachedComponentWidth;
    private double cachedComponentHeight;
    private int cachedBoardWidth;
    private int cachedBoardHeight;

    GameBoardSize() { }

    protected final void recompute(CoordinateLabelPosition newLabelPosition) {
        if (!cachedOnce) {
            throw new IllegalStateException("compute() must be invoked once first");
        }

        computeImpl(cachedComponentWidth, cachedComponentHeight,
                cachedBoardWidth, cachedBoardHeight,
                newLabelPosition, false);
    }

    /**
     * Recalculates the board sizing using a custom margin. The margin is the space between
     * the edges of the canvas to the start of the game board.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param boardWidth Number of board intersections horizontally
     * @param boardHeight Number of board intersections vertically
     */
    protected final void compute(double componentWidth, double componentHeight,
                                 int boardWidth, int boardHeight,
                                 CoordinateLabelPosition labelPos) {
        computeImpl(componentWidth, componentHeight, boardWidth, boardHeight, labelPos, true);
    }

    private void computeImpl(double componentWidth, double componentHeight,
                                     int boardWidth, int boardHeight,
                                     CoordinateLabelPosition coordinateLabelPosition,
                                     boolean cacheResult) {

        stageBounds = new Rectangle(0, 0, componentWidth, componentHeight);

        // Overview:
        // =========
        // Start by approximating a reasonable size of the go board based on the total
        // intersection width-to-height ratio, then we find the maximum rectangle inside
        // the stage of such ratio, which serves as the basis of the grid intersections.
        // This should give us a good starting point for estimating a square stone size
        // and board scale appropriate for the current stage dimensions.
        //
        // It is a lot easier to preserve rectangle proportions incrementally
        // (i.e. by adding spacing around an existing known size) than to subtract from
        // an existing component, we adopt the former approach. First we define the correct
        // ratio of the board intersection area (which ensures the stone size is square),
        // then we expand from this region to obtain co-ordinate label bounds, board bounds,
        // board border bounds etc. The sizing of these boundaries will overflow the viewport
        // (stage) at this point, but we will re-scale them at the final step, making the
        // top-level container fit within the stage (and rescale all inner components based
        // on the scale value).

        // We need to calculate a scaled version of stone size at this point to maintain
        // the correct board ratio
        var aspectRatio = (double) boardWidth / (double) boardHeight;
        Rectangle gridFitRatio = ShapeUtilities.centerFit(stageBounds, aspectRatio, 0d);

        // Doesn't really matter the size of the grid bounds at this point, the proportion
        // is what matters. We will scale everything to the correct size later.
        var scaledGridBounds = new LayoutRectangle(gridFitRatio.getWidth(), gridFitRatio.getHeight());
        var scaledStoneSize = (scaledGridBounds.getWidth() / boardWidth + scaledGridBounds.getHeight() / boardHeight) / 2;
        var stoneArea = scaledGridBounds.createParentWithMargin(scaledStoneSize);
        coordinateLabelBounds = computeCoordinateLabelBounds(stoneArea, scaledStoneSize, coordinateLabelPosition);

        boardBounds = coordinateLabelBounds.createParentWithMargin(getPixelValue(0.01d, coordinateLabelBounds));
        // Expressed as a percentage of total canvas width/height
        // Rescale everything. All the objects are referenced internally by
        // LayoutRectangle, so the entire hierarchy will be updated. These are percentages
        // of the shorter side of board dimensions rather than total size
        double percentageMarginFromEdge = 0.02d;
        double marginFromEdgeInPixels = percentageMarginFromEdge
                * Math.min(stageBounds.getWidth(), stageBounds.getHeight());
        boardBounds.rescaleAndFinalize(stageBounds.getX(), stageBounds.getY(),
                stageBounds.getWidth(), stageBounds.getHeight(), marginFromEdgeInPixels, 1.0d, true);

        // Now we can finally calculate the correct stone size
        double stoneSizeFromWidth = scaledGridBounds.getWidth() / ((boardWidth == 1) ? 1 : (boardWidth - 1));
        double stoneSizeFromHeight = scaledGridBounds.getHeight() / ((boardHeight == 1) ? 1 : (boardHeight - 1));

        gridUnitSize = Math.min(stoneSizeFromWidth, stoneSizeFromHeight);
        // Space between two adjacent stones, expressed as a percentage of grid unit size
        double percentageStoneGap = 0.01d;
        stoneGapSize = gridUnitSize * percentageStoneGap;
        stoneSize = gridUnitSize - stoneGapSize;

        // Now that we have the final stone size, we may need to clip from the original
        // scaled grid bounds to center the intersections, because the first round of
        // calculation was a rough estimate.
        double actualGridWidth = gridUnitSize * (boardWidth - 1);
        double actualGridHeight = gridUnitSize * (boardHeight - 1);

        double excessGridWidth = scaledGridBounds.getWidth() - actualGridWidth;
        double excessGridHeight = scaledGridBounds.getHeight() - actualGridHeight;

        double horizontalClip = excessGridWidth / 2;
        double verticalClip = excessGridHeight / 2;

        gridBounds = ShapeUtilities.clip(scaledGridBounds.getX(), scaledGridBounds.getY(),
                scaledGridBounds.getWidth(), scaledGridBounds.getHeight(),
                horizontalClip, verticalClip, horizontalClip, verticalClip);

        stoneShadowRadius = stoneSize / 8d;
        stoneShadowOffset = stoneSize / 16d;

        if (cacheResult) {
            cacheParameters(componentWidth, componentHeight, boardWidth, boardHeight);
        }
    }

    private void cacheParameters(double componentWidth, double componentHeight,
                                 int boardWidth, int boardHeight) {

        this.cachedComponentWidth = componentWidth;
        this.cachedComponentHeight = componentHeight;
        this.cachedBoardWidth = boardWidth;
        this.cachedBoardHeight = boardHeight;

        this.cachedOnce = true;
    }

    /*
        Determines how the grid boundaries are arranged within the game board based on the
        co-ordinate label orientation.
     */
    private LayoutRectangle computeCoordinateLabelBounds(LayoutRectangle gridBounds,
                                                         double stoneSize,
                                                         CoordinateLabelPosition coordinateLabelPosition) {
        double marginLeft = 0, marginTop = 0, marginBottom = 0, marginRight = 0;
        LayoutRectangle.ContainerStrategy containerStrategy;

        if (coordinateLabelPosition == CoordinateLabelPosition.NONE) {
            return gridBounds;
        } else if (coordinateLabelPosition == CoordinateLabelPosition.TOP_AND_LEFT) {
            containerStrategy = LayoutRectangle.ContainerStrategy.CLAMP_BOTTOM_RIGHT;
            marginBottom = stoneSize;
            marginRight = stoneSize;
        } else if (coordinateLabelPosition == CoordinateLabelPosition.BOTTOM_AND_RIGHT) {
            containerStrategy = LayoutRectangle.ContainerStrategy.CLAMP_TOP_LEFT;
            marginTop = stoneSize;
            marginLeft = stoneSize;
        } else if (coordinateLabelPosition == CoordinateLabelPosition.ALL_SIDES) {
            marginTop = stoneSize;
            marginLeft = stoneSize;
            marginBottom = stoneSize;
            marginRight = stoneSize;
            containerStrategy = LayoutRectangle.ContainerStrategy.CENTER;
        } else {
            throw new IllegalStateException("Unimplemented coordinate label position: " + coordinateLabelPosition);
        }

        return gridBounds.createParentWithMargin(marginLeft, marginTop, marginRight,
                marginBottom, containerStrategy);
    }

    public Rectangle getStageBounds() {
        return stageBounds;
    }

    public double getStoneShadowRadius() {
        return stoneShadowRadius;
    }

    public double getStoneShadowOffset() {
        return stoneShadowOffset;
    }

    public double[] getStoneRenderPosition(int gridX, int gridY) {
        return getGridRenderPosition(gridX, gridY, getStoneSizeInPixels());
    }

    /**
     * 
     * @see #getGridRenderPosition(int, int, double, double)
     */
    public double[] getGridRenderPosition(int gridX, int gridY, double objectSize) {
        return getGridRenderPosition(gridX, gridY, objectSize, objectSize);
    }

    /**
     * Returns the drawing position of logical position (gridX, gridY) for an object of
     * size objectWidth by objectHeight. The returned position will automatically be
     * adjusted such that drawing an object of this size will make the intersection
     * at the center of the object.
     * <p/>
     * To convert a drawing position to logical board position, see
     * {@link #getGridPosition(double, double)}
     *
     * @param gridX Logical x position on the game board
     * @param gridY Logical y position on the game board
     * @param objectWidth The draw width of the object on this intersection
     * @param objectHeight The draw height of the object on this intersection
     * @return Size 2 array consisting of { drawX, drawY } co-ordinates in that order
     */
    public double[] getGridRenderPosition(int gridX, int gridY, double objectWidth, double objectHeight) {
        Rectangle gridBounds = getGridBounds();

        double x = gridBounds.getX() + getGridUnitSizeInPixels() * gridX - objectWidth / 2;
        double y = gridBounds.getY() + getGridUnitSizeInPixels() * gridY - objectHeight / 2;

        return new double[] { x, y };
    }

    /**
     * From a given render (or mouse) position, returns the logical board intersection
     * the position corresponds to.
     *
     * @param x Drawn x position
     * @param y Drawn y position
     * @return The logical board intersection represented by the provided draw position,
     *         or {@link Optional#empty()} if the position is not within grid boundaries.
     */
    public Optional<int[]> getGridPosition(double x, double y) {
        Rectangle gridBounds = getGridBounds();

        double normalizedX = x - gridBounds.getX();
        double normalizedY = y - gridBounds.getY();

        if (normalizedX < -getGridUnitSizeInPixels() / 2
                || normalizedY < -getGridUnitSizeInPixels() / 2
                || normalizedX > gridBounds.getWidth() + getGridUnitSizeInPixels() / 2
                || normalizedY > gridBounds.getHeight() + getGridUnitSizeInPixels() / 2) {
            return Optional.empty();
        }

        int gridX = (int) Math.round(normalizedX / getGridUnitSizeInPixels());
        int gridY = (int) Math.round(normalizedY / getGridUnitSizeInPixels());

        int[] gridPosition = new int[] { gridX, gridY };

        return Optional.of(gridPosition);
    }

    /**
     *
     * @return The drawing boundaries for the game board image
     */
    public Rectangle getBoardBounds() {
        return boardBounds;
    }

    /**
     *
     * @return The drawing boundaries for the game co-ordinates
     */
    public Rectangle getCoordinateLabelBounds() {
        return coordinateLabelBounds;
    }

    /**
     *
     * @return The drawing boundaries for the grid of intersections.
     */
    public Rectangle getGridBounds() {
        return gridBounds;
    }

    /**
     *
     * @return The blur factor of the shadow effect used to paint board component border
     */
    public double getShadowRadius() {
        final double percentageShadowBlurRadius = 0.05d;
        return percentageShadowBlurRadius * getBoardBoundsPercentageMetric();
    }

    /**
     *
     * @return The amount of offset in both x and y direction of the board border shadow
     */
    public double getBoardBorderShadowOffsetInPixels() {
        double percentageBoardShadowOffset = 0.012d;
        return percentageBoardShadowOffset * getBoardBounds().getHeight();
    }

    /**
     *
     * @return The side of {@link #boardBounds} used to calculate percentages related to
     * the board component.
     */
    private double getBoardBoundsPercentageMetric() {
        return Math.min(boardBounds.getWidth(), boardBounds.getHeight());
    }

    /**
     *
     * @return The square dimension of a rendered Go stone, in pixels.
     */
    public double getStoneSizeInPixels() {
        return stoneSize;
    }

    /**
     *
     * @return The thickness, in pixel units, of the line used to draw the board grids.
     */
    public double getGridLineThicknessInPixels() {
        double percentageGridLineThickness = 0.00125d;
        double thickness = percentageGridLineThickness * getBoardBoundsPercentageMetric();
        return Math.max(1d, thickness);
    }

    public double getStoneGapSizeInPixels() {
        return stoneGapSize;
    }

    public double getGridUnitSizeInPixels() {
        return gridUnitSize;
    }

    private double getPixelValue(double percentage, Rectangle rectangle) {
        return percentage * Math.min(rectangle.getWidth(), rectangle.getHeight());
    }

    public boolean hasComputedOnce() {
        return cachedOnce;
    }

    /**
     * A rectangle with one child hierarchy. The child rectangle is assumed to be fully
     * contained within this rectangle. When the parent rectangle is rescaled, the child
     * will also be rescaled accordingly.
     * <p/>
     * Each rectangle should have a (x, y) position at (0, 0).
     */
    private static class LayoutRectangle extends Rectangle {

        enum ContainerStrategy {
            CENTER,
            CLAMP_BOTTOM_RIGHT,
            CLAMP_TOP_LEFT
        }

        private LayoutRectangle child;

        private double marginLeft = 0;
        private double marginTop = 0;
        private double marginRight = 0;
        private double marginBottom = 0;
        private ContainerStrategy containerStrategy = ContainerStrategy.CENTER;

        private boolean finalized = false;

        public LayoutRectangle(double width, double height) {
            super(width, height);
        }

        /**
         *
         * @see #createParentWithMargin(double, double, double, double, ContainerStrategy)
         */
        public LayoutRectangle createParentWithMargin(double marginOnAllSides) {
            return createParentWithMargin(marginOnAllSides, marginOnAllSides,
                    marginOnAllSides, marginOnAllSides);
        }

        /**
         * 
         * @see #createParentWithMargin(double, double, double, double, ContainerStrategy)
         */
        public LayoutRectangle createParentWithMargin(double marginLeft, double marginTop,
                                                      double marginRight, double marginBottom) {
            return createParentWithMargin(marginLeft, marginTop, marginRight, marginBottom,
                    ContainerStrategy.CENTER);
        }

        /**
         * Create and return a parent for this rectangle whose child is set to this instance.
         * The parent will fully contain the child rectangle and have its position starting
         * at (0, 0). The child position will be updated with the margin data depending on the
         * container strategy.
         *
         * @param marginLeft Space to the left of the child before reaching left edge of parent
         * @param marginTop Space above the child before reaching top edge of parent
         * @param marginRight Space to the right of the child before reaching the right edge of parent
         * @param marginBottom Space to the bottom of the child before reaching the bottom edge of parent
         * @param strategy How the child fits inside the parent. It is set to
         *                 {@link ContainerStrategy#CENTER} by default, which means the
         *                 child will be centered in the parent bounds.
         *
         * @return The parent rectangle for this instance.
         */
        private LayoutRectangle createParentWithMargin(double marginLeft, double marginTop,
                                                       double marginRight, double marginBottom,
                                                       ContainerStrategy strategy) {
            assertNotFinalized();

            this.marginLeft = marginLeft;
            this.marginTop = marginTop;
            this.marginRight = marginRight;
            this.marginBottom = marginBottom;
            this.containerStrategy = strategy;

            var parent = new LayoutRectangle(getWidth() + marginLeft + marginRight, getHeight() + marginTop + marginBottom);

            switch (strategy) {
                case CENTER:
                    offsetContents(marginLeft, marginTop);
                    break;
                case CLAMP_BOTTOM_RIGHT:
                    offsetContents(marginLeft + marginRight, marginTop + marginBottom);
                    break;
                case CLAMP_TOP_LEFT:
                    // No offset
                    break;
                default:
                    throw new IllegalStateException("Unsupported container strategy: " + strategy);
            }

            parent.child = this;

            return parent;
        }

        private void offsetContents(double x, double y) {
            assertNotFinalized();

            setX(getX() + x);
            setY(getY() + y);

            if (child != null) {
                child.offsetContents(x, y);
            }
        }

        /**
         * Scales all the contents within this rectangle to an appropriate size while
         * preserving width to height ratio and centers the scaled version inside the
         * component bounds.
         * <p/>
         * Additionally, marks this rectangle and its entire sub-hierarchy as finalized.
         * This means no more changes can be made to this rectangle and its hierarchy
         * after this operation.
         *
         * @param containerX Container bounds to center this component within
         * @param containerY Container bounds to center this component within
         * @param containerWidth Container bounds to center this component within
         * @param containerHeight Container bounds to center this component within
         * @param parentScale Scale used to transform parent rectangle dimensions
         */
        public void rescaleAndFinalize(double containerX, double containerY,
                                       double containerWidth, double containerHeight,
                                       double margin, double parentScale,
                                       boolean firstIteration) {
            assertNotFinalized();

            double originalWidth = getWidth();
            double widthToHeightRatio = getWidth() / getHeight();

            this.marginLeft *= parentScale;
            this.marginTop *= parentScale;
            this.marginRight *= parentScale;
            this.marginBottom *= parentScale;

            double fitContainerX = containerX + marginLeft + margin;
            double fitContainerY = containerY + marginTop + margin;
            double fitContainerWidth = containerWidth - (marginLeft + marginRight) - margin * 2;
            double fitContainerHeight = containerHeight - (marginTop + marginBottom) - margin * 2;
            Rectangle fitContainerBounds = new Rectangle(fitContainerX, fitContainerY, fitContainerWidth, fitContainerHeight);

            Rectangle fitBounds = ShapeUtilities.centerFit(fitContainerBounds, widthToHeightRatio, 0);
            double fitScale = fitBounds.getWidth() / originalWidth;

            setWidth(fitBounds.getWidth());
            setHeight(fitBounds.getHeight());

            switch (containerStrategy) {
                case CENTER:
                    setX(fitBounds.getX());
                    setY(fitBounds.getY());
                    break;
                case CLAMP_TOP_LEFT:
                    // If first iteration, it will clamp to (0, 0) of the stage if we don't snap to fitBounds.
                    if (firstIteration) {
                        setX(fitBounds.getWidth());
                        setY(fitBounds.getHeight());
                    } else {
                        setX(containerX);
                        setY(containerY);
                    }
                    break;
                case CLAMP_BOTTOM_RIGHT:
                    setX(containerX + containerWidth - fitBounds.getWidth());
                    setY(containerY + containerHeight - fitBounds.getHeight());
                    break;
                default:
                    throw new IllegalStateException("Unsupported container strategy: " + containerStrategy);
            }

            finalized = true;

            if (child != null) {
                child.rescaleAndFinalize(getX(), getY(), this.getWidth(), this.getHeight(), 0, fitScale, false);
            }
        }

        private void assertNotFinalized() {
            assert !finalized : "Cannot modify layout rectangle after it has been finalized!";
        }
    }
}
