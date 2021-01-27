package yi.component.boardviewer;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import yi.component.shared.component.FontManager;
import yi.component.shared.utilities.ComparisonUtilities;
import yi.core.go.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Draws the primary contents of the game board, such as the board image, grids, star points,
 * stones and annotations that are part of the game state. In principle, any object that isn't
 * expected to be repainted very quickly should be drawn here.
 * <p/>
 * For quick-repaint objects, use {@link GameBoardInputCanvas}.
 */
final class GameBoardMainCanvas extends GameBoardCanvas {

    GameBoardMainCanvas(GameBoardManager manager) {
        super(manager);
    }

    @Override
    protected void renderImpl(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());

        BoardRenderer.render(g, manager);
        BoardStoneRenderer.render(g, manager);
        BoardAnnotationRenderer.render(g, manager);
    }

    private static final class BoardRenderer {

        private static final DropShadow BOARD_BORDER_SHADOW = new DropShadow();

        private static void render(GraphicsContext g, GameBoardManager manager) {
            renderBackground(g, manager);
            renderBoardTexture(g, manager);
            renderCoordinateLabels(g, manager);
            renderGrid(g, manager);

            if (manager.isDebugMode()) {
                renderDebug(g, manager);
            }
        }

        private static void renderDebug(GraphicsContext g, GameBoardManager manager) {
            var size = manager.size;

            var stage = size.getStageBounds();
            g.setStroke(Color.RED);
            g.setLineWidth(1d);
            g.strokeRect(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

            var grid = size.getGridBounds();
            g.setStroke(Color.BLUE);
            g.strokeRect(grid.getX(), grid.getY(), grid.getWidth(), grid.getHeight());

            var board = size.getBoardBounds();
            g.setStroke(Color.MAGENTA);
            g.strokeRect(board.getX(), board.getY(), board.getWidth(), board.getHeight());

            var coordinates = size.getCoordinateLabelBounds();
            g.setStroke(Color.PURPLE);
            g.strokeRect(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight());
        }

        private static void renderBackground(GraphicsContext g, GameBoardManager manager) {
            Image backgroundImage = manager.view.backgroundImage;

            if (backgroundImage != null) {
                Rectangle stage = manager.size.getStageBounds();
                assert stage != null : "stage == null, did size calculation not run?";
                g.drawImage(backgroundImage, stage.getX(), stage.getY(), stage.getWidth(),
                        stage.getHeight());
            }
        }

        private static void renderBoardTexture(GraphicsContext g, GameBoardManager manager) {
            double borderOffset = manager.size.getBoardBorderShadowOffsetInPixels();
            BOARD_BORDER_SHADOW.setRadius(manager.size.getShadowRadius());
            BOARD_BORDER_SHADOW.setOffsetY(borderOffset);
            BOARD_BORDER_SHADOW.setColor(Color.color(0d, 0d, 0d, 0.7d));
            g.setEffect(BOARD_BORDER_SHADOW);

            Rectangle boardBounds = manager.size.getBoardBounds();
            g.drawImage(manager.view.boardImage, boardBounds.getX(), boardBounds.getY(),
                    boardBounds.getWidth(), boardBounds.getHeight());

            g.setEffect(null);

            { // Draw border around board edge
                g.setStroke(Color.BLACK);
                g.setGlobalAlpha(0.1d);
                final double borderThickness = boardBounds.getWidth() / 100d;
                g.setLineWidth(borderThickness);
                g.strokeRect(boardBounds.getX() + borderThickness / 2,
                        boardBounds.getY() + borderThickness / 2,
                        boardBounds.getWidth() - borderThickness,
                        boardBounds.getHeight() - borderThickness);
                g.setGlobalAlpha(1.0d);
            }

        }

        private static final String COORDINATE_X_AXIS_TEXT = "ABCDEFGHJKLMNOPQRSTUVWXYZ";

        private static void renderCoordinateLabels(GraphicsContext g, GameBoardManager manager) {
            var boardWidth = manager.getGameModel().getBoardWidth();
            var boardHeight = manager.getGameModel().getBoardHeight();

            var pos = manager.view.coordinateLabelPosition;
            var drawTop = pos == CoordinateLabelPosition.ALL_SIDES || pos == CoordinateLabelPosition.TOP_AND_LEFT;
            var drawLeft = pos == CoordinateLabelPosition.ALL_SIDES || pos == CoordinateLabelPosition.TOP_AND_LEFT;
            var drawRight = pos == CoordinateLabelPosition.ALL_SIDES || pos == CoordinateLabelPosition.BOTTOM_AND_RIGHT;
            var drawBottom = pos == CoordinateLabelPosition.ALL_SIDES || pos == CoordinateLabelPosition.BOTTOM_AND_RIGHT;

            var coordBounds = manager.size.getCoordinateLabelBounds();
            var gridBounds = manager.size.getGridBounds();

            var maxHeightForLabels = (coordBounds.getHeight() - gridBounds.getHeight()) / 2;
            var maxWidthForLabels = (coordBounds.getWidth() - gridBounds.getWidth()) / 2;

            var heightFontSize = maxHeightForLabels / 3;
            var widthFontSize = maxWidthForLabels / 3;
            var fontSize = Math.min(heightFontSize, widthFontSize);
            if (fontSize < 1d) {
                fontSize = 1d;
            }

            var font = FontManager.getCachedFont(BoardRenderer.class)
                    .orElse(createAndCacheCoordinateLabelFont(fontSize));

            if (!ComparisonUtilities.doubleEquals(font.getSize(), fontSize)) {
                font = createAndCacheCoordinateLabelFont(fontSize);
            }

            g.setFont(font);
            g.setFill(manager.view.boardGridColor.darker());

            var textBounds = getTextBounds("A", font);
            if (drawTop) {
                var y = coordBounds.getY()
                        + fontSize
                        + maxHeightForLabels / 9 * 3
                        - textBounds.getHeight() / 2;

                renderHorizontalAxisLabels(g, manager.size, boardWidth, y, font);
            }
            if (drawBottom) {
                var y = coordBounds.getY()
                        + coordBounds.getHeight()
                        + (heightFontSize+1)
                        - maxHeightForLabels / 9 * 3
                        - textBounds.getHeight() / 2;

                renderHorizontalAxisLabels(g, manager.size, boardWidth, y, font);
            }

            if (drawLeft) {
                var x = coordBounds.getX()
                        + maxWidthForLabels / 3
                        - textBounds.getWidth() / 2;

                renderVerticalAxisLabels(g, manager.size, boardHeight, x, font);
            }

            if (drawRight) {
                var x = coordBounds.getX()
                        + coordBounds.getWidth()
                        - maxWidthForLabels / 3
                        - textBounds.getWidth() / 2;

                renderVerticalAxisLabels(g, manager.size, boardHeight, x, font);
            }
        }

        private static void renderVerticalAxisLabels(GraphicsContext g, GameBoardSize size, int boardHeight, double x, Font font) {
            for (int row = 0; row < boardHeight; ++row) {
                var gridPos = size.getGridRenderPosition(0, row, 0);
                var text = String.valueOf(boardHeight - row);
                var textBounds = getTextBounds(text, font);
                var adjustedX = x;
                if (text.length() > 1) {
                    adjustedX -= textBounds.getWidth() / 4;
                }
                var y = gridPos[1] - textBounds.getHeight() / 2 + font.getSize();

                g.fillText(text, adjustedX, y);
            }
        }

        private static void renderHorizontalAxisLabels(GraphicsContext g, GameBoardSize size, int boardWidth, double y, Font font) {
            for (int col = 0; col < boardWidth; ++col) {
                var gridPos = size.getGridRenderPosition(col, 0, 0);
                var text = String.valueOf(COORDINATE_X_AXIS_TEXT.charAt(col));
                var textBounds = getTextBounds(text, font);
                var x = gridPos[0] - textBounds.getWidth() / 2;

                g.fillText(text, x, y);
            }
        }

        private static Font createAndCacheCoordinateLabelFont(double fontSize) {
            var newFont = FontManager.getDefaultFont(fontSize);
            FontManager.putCachedFont(BoardRenderer.class, newFont);
            return newFont;
        }

        private static Bounds getTextBounds(String text, Font font) {
            var boundsTest = new Text(text);
            boundsTest.setFont(font);
            return boundsTest.getBoundsInLocal();
        }

        private static void renderGrid(GraphicsContext g, GameBoardManager manager) {
            double originalLineWidth = g.getLineWidth();
            double gridLineThickness = manager.size.getGridLineThicknessInPixels();

            // Draw game grid
            g.setLineWidth(gridLineThickness);

            Color lineColor = manager.view.boardGridColor;
            g.setFill(lineColor);
            g.setStroke(lineColor);

            Rectangle gridBounds = manager.size.getGridBounds();

            for (int lineNumber = 0; lineNumber < manager.getGameModel().getBoardWidth(); ++lineNumber) {
                double[] drawXY = manager.size.getGridRenderPosition(lineNumber, 0, 0);
                double x = drawXY[0];

                g.strokeLine(x, gridBounds.getY(), x, gridBounds.getY() + gridBounds.getHeight());
            }

            for (int lineNumber = 0; lineNumber < manager.getGameModel().getBoardHeight(); ++lineNumber) {
                double[] drawXY = manager.size.getGridRenderPosition(0, lineNumber, 0);
                double y = drawXY[1];

                g.strokeLine(gridBounds.getX(), y, gridBounds.getX() + gridBounds.getWidth(), y);
            }

            renderStarPoints(g, manager, gridLineThickness);

            g.setLineWidth(originalLineWidth);
        }

        private static void renderStarPoints(GraphicsContext g, GameBoardManager manager, double lineWidth) {
            // Number of intersections on the game board, not sizing
            int gameBoardWidth = manager.getGameModel().getBoardWidth();
            int gameBoardHeight = manager.getGameModel().getBoardHeight();

            var starPointPositions = StarPointPosition.get(gameBoardWidth, gameBoardHeight);
            double starPointDiameter = lineWidth * 6d;

            for (var starPointPosition : starPointPositions) {
                int x = starPointPosition % gameBoardWidth;
                int y = starPointPosition / gameBoardWidth;

                double[] drawXY = manager.size.getGridRenderPosition(x, y, starPointDiameter);

                g.fillOval(drawXY[0], drawXY[1], starPointDiameter, starPointDiameter);
            }
        }

        enum StarPointPosition {

            _19x19(point(3,  3, 19), point(9,  3, 19), point(15,  3, 19),
                   point(3,  9, 19), point(9,  9, 19), point(15,  9, 19),
                   point(3, 15, 19), point(9, 15, 19), point(15, 15, 19)),


            _13x13(point(3,  3, 13),                   point(9, 3, 13),
                                     point(6,  6, 13),
                   point(3,  9, 13),                   point(9, 9, 13)),


              _9x9(point(2, 2, 9),                     point(6, 2, 9),
                                     point(4,  4, 9),
                   point(2, 6, 9),                     point(6, 6, 9));


            private final Set<Integer> points;

            StarPointPosition(Integer ... points) {
                this.points = Arrays.stream(points).collect(Collectors.toSet());
            }

            static int point(int x, int y, int boardWidth) {
                return x + y * boardWidth;
            }

            static Set<Integer> get(int boardWidth, int boardHeight) {
                StarPointPosition configuration = null;

                if (boardWidth == boardHeight) {
                    if (boardWidth == 19)
                        configuration = _19x19;
                    else if (boardWidth == 13)
                        configuration = _13x13;
                    else if (boardWidth == 9)
                        configuration = _9x9;
                }

                if (configuration == null) {
                    return getDefault(boardWidth, boardHeight);
                } else {
                    return configuration.points;
                }
            }

            private static Set<Integer> getDefault(int boardWidth, int boardHeight) {
                // If the dimension is odd, return the centre intersection as a star point
                if (boardWidth % 2 == 1 && boardHeight % 2 == 1) {
                    int x = (boardWidth - 1) / 2;
                    int y = (boardHeight - 1) / 2;
                    return Collections.singleton(x + y * boardWidth);
                }

                return Collections.emptySet();
            }
        }
    }

    @Override
    public void onGameModelSet(GameModel newModel, GameBoardManager manager) {

    }

    @Override
    public void onGameUpdate(GameModel gameModel, GameBoardManager manager) {
        render(manager);
    }

    private static final class BoardStoneRenderer {

        public static void render(GraphicsContext g, GameBoardManager manager) {
            var nodeToShow = manager.getNodeToShow();
            var stateAtThatNode = manager.getGameModel().getGameState(nodeToShow);
            var boardPosition = stateAtThatNode.getBoardPosition();

            StoneColor[] boardState = boardPosition.getIntersectionState();
            int boardWidth = manager.getGameModel().getBoardWidth();

            for (int i = 0; i < boardState.length; ++i) {
                StoneColor state = boardState[i];

                int x = i % boardWidth;
                int y = i / boardWidth;

                // TODO: Temporary. Allow rendering custom stone images in the future.
                PresetStoneStyle.getDefaultValue().render(g, manager, state, x, y);
            }
        }
    }

    private static final class BoardAnnotationRenderer {

        public static void render(GraphicsContext g, GameBoardManager manager) {
            if (manager.isShowingCurrentPosition()) {
                renderAnnotationsOnCurrentMove(g, manager);
            } else {
                // Previewing
                renderPreviewAnnotations(g, manager);
            }
        }

        private static void renderPreviewAnnotations(GraphicsContext g, GameBoardManager manager) {
            var current = manager.getGameModel().getCurrentNode();
            var shown = manager.getNodeToShow();

            assert current != shown;

            if (shown.getMoveNumber() > current.getMoveNumber()
                    && shown.getMoveHistory().contains(current)) {
                renderMoveNumbersUpToPreviewNode(current, shown, g, manager);
            } else {
                renderMoveMarker(shown, g, manager);
            }
        }

        private static void renderMoveNumbersUpToPreviewNode(GameNode current, GameNode shown,
                                                             GraphicsContext g,
                                                             GameBoardManager manager) {
            List<GameNode> historyToShownNode = new LinkedList<>(shown.getMoveHistory());
            assert historyToShownNode.size() >= 1;
            historyToShownNode.remove(0);
            historyToShownNode.subList(0, current.getMoveNumber()).clear();

            var font = getLabelFont(manager);
            int step = 1;

            // Only want the last annotation on that intersection to show up
            // TODO: Find somewhere to represent the ko re-captures whose move number
            //       did not show because a later move was played on the same spot.
            var intersectionWithAnnotations = new HashMap<Integer, Annotation>();

            for (GameNode node : historyToShownNode) {
                Stone move = node.getPrimaryMove();
                if (move != null) {
                    var x = move.getX();
                    var y = move.getY();
                    intersectionWithAnnotations.put(y * manager.getGameModel().getBoardHeight() + x,
                            new Annotation.Label(x, y, String.valueOf(step)));
                } else {
                    break;
                }
                ++step;
            }

            for (Integer position : intersectionWithAnnotations.keySet()) {
                AnnotationRenderer.render(intersectionWithAnnotations.get(position), g, manager, font);
            }
        }

        private static Font getLabelFont(GameBoardManager manager) {
            return AnnotationRenderer.getAndCacheLabelFont(manager.size, BoardAnnotationRenderer.class);
        }

        private static void renderAnnotationsOnCurrentMove(GraphicsContext g, GameBoardManager manager) {
            // Not using a copy because this is performance-sensitive code
            Collection<Annotation> annotations = manager.getGameModel().getCurrentNode().getAnnotations();
            var font = getLabelFont(manager);

            for (Annotation annotation : annotations) {
                AnnotationRenderer.render(annotation, g, manager, font);
            }

            renderCurrentMoveMarker(g, manager);
            renderNextMoveMarkers(g, manager);
        }

        private static void renderCurrentMoveMarker(GraphicsContext g, GameBoardManager manager) {
            renderMoveMarker(manager.getGameModel().getCurrentNode(), g, manager);
        }

        private static void renderMoveMarker(GameNode nodeToMark, GraphicsContext g, GameBoardManager manager) {
            var primaryMove = nodeToMark.getPrimaryMove();
            if (primaryMove != null) {
                int x = primaryMove.getX();
                int y = primaryMove.getY();

                if (!manager.getGameModel().getCurrentNode().hasAnnotationAt(x, y)) {
                    AnnotationRenderer.render(new Annotation.Dot(x, y), g, manager, null);
                }
            }
        }

        private static final Color NEXT_MOVE_BLACK_MARKER = new Color(0d, 0d, 0d, 0.6d);
        private static final Color NEXT_MOVE_WHITE_MARKER = new Color(1d, 1d, 1d, 0.6d);

        private static void renderNextMoveMarkers(GraphicsContext g, GameBoardManager manager) {
            var model = manager.getGameModel();
            var currentNode = model.getCurrentNode();
            var children = currentNode.getChildNodes();

            var primaryMoveVariations = 0;
            for (GameNode child : children) {
                if (child.getPrimaryMove() != null) {
                    ++primaryMoveVariations;
                }
                if (primaryMoveVariations > 1) {
                    break;
                }
            }

            // TODO: OGS-style AI-review branches are not supported in the next move marker
            //       because it uses a stone-edit node for each AI move.
            if (primaryMoveVariations > 1) {
                for (GameNode child : children) {
                    Stone primaryMove = child.getPrimaryMove();
                    boolean drawIt = false;
                    int x = -1;
                    int y = -1;

                    StoneColor color = null;

                    if (primaryMove != null) {
                        color = primaryMove.getColor();
                        x = primaryMove.getX();
                        y = primaryMove.getY();
                    }

                    if (color == StoneColor.BLACK) {
                        g.setFill(NEXT_MOVE_BLACK_MARKER);
                        drawIt = true;
                    } else if (color == StoneColor.WHITE) {
                        g.setFill(NEXT_MOVE_WHITE_MARKER);
                        drawIt = true;
                    }

                    if (drawIt) {
                        assert x != -1 && y != -1;

                        var size = manager.size.getStoneSizeInPixels() / 2d;
                        double[] pos = manager.size.getGridRenderPosition(x, y, size);
                        var drawX = pos[0];
                        var drawY = pos[1];
                        g.fillOval(drawX, drawY, size, size);
                    }
                }
            }
        }
    }
}
