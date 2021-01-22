package yi.component.treeviewer;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.GuiUtilities;
import yi.core.go.GameNode;
import yi.core.go.GameNodeType;

import java.util.List;

/**
 * Manages the rendering of the game tree.
 *
 * See {@link GameTreeViewer.CanvasInputHandler} for input handling.
 */
final class GameTreeCanvas extends Canvas {

    private static final double BRANCH_LINE_WIDTH = 2d;

    private final GraphicsContext graphics;

    public GameTreeCanvas() {
        graphics = getGraphicsContext2D();
    }

    public void addInputHandler(InputHandler handler) {
        addEventHandler(MouseEvent.MOUSE_MOVED, handler::mouseMoved);
        addEventHandler(MouseEvent.MOUSE_PRESSED, handler::mousePressed);
        addEventHandler(MouseEvent.MOUSE_CLICKED, handler::mouseClicked);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, handler::mouseDragged);
        addEventHandler(MouseEvent.MOUSE_ENTERED, handler::mouseEntered);
        addEventHandler(MouseEvent.MOUSE_EXITED, handler::mouseExited);
        addEventHandler(ScrollEvent.SCROLL, handler::mouseScrolled);
        addEventHandler(KeyEvent.KEY_PRESSED, handler::keyPressed);
    }

    public void render(GameTreeViewerSettings settings, Camera camera,
                       List<TreeNodeElement> visibleElements, GameNode currentNode,
                       @Nullable GameNode previewNode,
                       GameTreeElementSize size) {

        graphics.setFont(settings.getPreviewTextFont());
        var currentVariationHistory = currentNode.getMoveHistory();

        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setFill(settings.getBackgroundColor());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        graphics.setStroke(settings.getNodeColor());
        graphics.setLineWidth(BRANCH_LINE_WIDTH);
        graphics.setFill(settings.getNodeColor());

        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        final double gridWidth = size.getGridSize().getWidth();
        final double gridHeight = size.getGridSize().getHeight();

        renderTracks(settings, visibleElements, currentVariationHistory, previewNode, gridWidth, gridHeight, offsetX, offsetY);
        renderNodes(settings, visibleElements, currentNode, currentVariationHistory, previewNode, gridWidth, gridHeight, offsetX, offsetY);

        if (settings.isPreviewPromptEnabled()) {
            renderPreviewPrompt(settings, previewNode);
        }
    }

    private void renderPreviewPrompt(GameTreeViewerSettings settings, @Nullable GameNode previewNode) {
        final int PROMPT_HEIGHT = 30;

        if (previewNode != null) {
            graphics.setFill(settings.getPreviewPromptBackground());
            graphics.fillRect(0, getHeight() - PROMPT_HEIGHT, getWidth(), PROMPT_HEIGHT);
            graphics.setFill(settings.getPreviewPromptForeground());

            TextResource previewText =  settings.getPreviewTextResource();
            if (previewText != null) {
                var font = graphics.getFont();
                var moveNumber = previewNode.getMoveNumber();
                var localizedText = previewText.getLocalisedText(moveNumber);

                Bounds bounds = GuiUtilities.getTextBoundsLocal(font, localizedText);
                double x = getWidth() / 2 - bounds.getWidth() / 2;
                double y = (getHeight() - PROMPT_HEIGHT / 2d) - bounds.getHeight() / 2 + font.getSize();

                graphics.fillText(localizedText, x, y);
            }
        }
    }

    private void renderNodes(GameTreeViewerSettings settings, List<TreeNodeElement> nodeElements,
                             GameNode currentNode, List<GameNode> currentVariationHistory,
                             @Nullable GameNode previewNode,
                             double gridWidth, double gridHeight, double offsetX, double offsetY) {

        var previewNodeHistory = previewNode != null ? previewNode.getMoveHistory() : null;

        for (var nodeElement : nodeElements) {
            double x = nodeElement.getGridX() * gridWidth + offsetX;
            double y = nodeElement.getGridY() * gridHeight + offsetY;

            var nodeColor = settings.getNodeColor();

            var node = nodeElement.getNode();
            var isCurrentNode = node.equals(currentNode);
            var isPartOfCurrentHistory = currentVariationHistory.contains(node);
            var isCommented = !node.getComments().isBlank();
            var isPass = node.getType() == GameNodeType.PASS;
            var strokeOutlineForCurrentNode = false;

            if (isCurrentNode) {
                nodeColor = settings.getCurrentNodeColor();
            } else if (isPartOfCurrentHistory) {
                nodeColor = settings.getNodeInCurrentVariationColor();
            } else if (isPass) {
                nodeColor = settings.getNodePassColor();
            }

            if (isCommented) {
                nodeColor = settings.getNodeWithCommentaryColor();
                if (isPartOfCurrentHistory) {
                    nodeColor = nodeColor.brighter();
                }
                strokeOutlineForCurrentNode = true;
            }

            var insets = isCurrentNode ? 3 : 5;

            if (node.getType() == GameNodeType.STONE_EDIT) {
                insets -= 1; // Optical illusion, diamond appears slightly smaller at same insets
            }

            if (!isPartOfCurrentHistory && previewNodeHistory != null
                    && previewNodeHistory.contains(node)) {
                nodeColor = nodeColor.brighter();
                insets -= 1;
            }

            if (nodeElement.isHighlighted()) {
                nodeColor = nodeColor.brighter();
                insets -= 1;
            }

            graphics.setFill(nodeColor);
            graphics.setStroke(nodeColor);

            var bounds = new Rectangle(x + insets, y + insets, gridWidth - insets * 2, gridHeight - insets * 2);
            settings.setNodeWithCommentaryColor(GuiUtilities.getColor(74, 110, 145));

            if (node.isRoot()) {
                graphics.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                if (isCurrentNode && strokeOutlineForCurrentNode) {
                    graphics.setStroke(settings.getCurrentNodeColor());
                    graphics.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                }
            } else {
                switch (node.getType()) {
                    case PASS:
                        graphics.setFill(settings.getBackgroundColor());
                        graphics.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                        graphics.strokeOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                        break;
                    case STONE_EDIT:
                        // Diamond
                        var xMid = bounds.getX() + bounds.getWidth() / 2;
                        var yMid = bounds.getY() + bounds.getHeight() / 2;
                        double[] xPoints = {
                                bounds.getX(), xMid, bounds.getX() + bounds.getWidth(), xMid
                        };
                        double[] yPoints = {
                                yMid, bounds.getY(), yMid, bounds.getY() + bounds.getHeight()
                        };

                        graphics.fillPolygon(xPoints, yPoints, xPoints.length);
                        if (isCurrentNode && strokeOutlineForCurrentNode) {
                            graphics.setStroke(settings.getCurrentNodeColor());
                            graphics.strokePolygon(xPoints, yPoints, xPoints.length);
                        }
                        break;
                    default:
                        graphics.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

                        if (isCurrentNode && strokeOutlineForCurrentNode) {
                            graphics.setStroke(settings.getCurrentNodeColor());
                            graphics.strokeOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                        }
                        break;
                }

            }
        }
    }

    private void renderTracks(GameTreeViewerSettings settings, List<TreeNodeElement> nodeElements,
                              List<GameNode> currentVariationHistory, @Nullable GameNode previewNode,
                              double gridWidth, double gridHeight, double offsetX, double offsetY) {

        var previewNodeHistory = previewNode != null ? previewNode.getMoveHistory() : null;

        for (int i = nodeElements.size() - 1; i > 0; --i) {
            var nodeElement = nodeElements.get(i);

            nodeElement.getParent().ifPresent(parent -> {
                double px = parent.getGridX() * gridWidth;
                double py = parent.getGridY() * gridHeight;

                double pCenterX = px + gridWidth / 2d + offsetX;
                double pCenterY = py + gridHeight / 2d + offsetY;

                double x = nodeElement.getGridX() * gridWidth;
                double y = nodeElement.getGridY() * gridHeight;

                double centerX = x + gridWidth / 2d + offsetX;
                double centerY = y + gridHeight / 2d + offsetY;

                var parentNode = parent.getNode();
                var thisNode = nodeElement.getNode();

                Color trackColor;

                if (currentVariationHistory.contains(thisNode)) {
                    trackColor = settings.getNodeInCurrentVariationColor();
                } else if (previewNodeHistory != null && previewNodeHistory.contains(thisNode)) {
                    trackColor = settings.getNodeColor().brighter();
                } else {
                    trackColor = settings.getNodeColor();
                }

                graphics.setStroke(trackColor);

                // No need to re-draw the top branch line since the last child draws it first.
                // This way we also avoid node branches that are not the current variation drawing over the
                // current variation branch color.
                boolean drawBranchLine = false;

                if (currentVariationHistory.contains(parentNode)) {
                    var parentIndexInHistory = currentVariationHistory.indexOf(parentNode);
                    var continuationNode = parentIndexInHistory + 1 < currentVariationHistory.size()
                            ? currentVariationHistory.get(parentIndexInHistory + 1) : null;

                    if (continuationNode != null) {
                        var continuationOrder = parentNode.getChildOrder(continuationNode);
                        var thisOrder = parentNode.getChildOrder(thisNode);

                        if (thisOrder >= continuationOrder) {
                            drawBranchLine = true;
                        }
                    } else {
                        drawBranchLine = true;
                    }
                } else {
                    drawBranchLine = true;
                }

                if (drawBranchLine) {
                    graphics.strokeLine(pCenterX, pCenterY, centerX, pCenterY);
                }

                graphics.strokeLine(centerX, pCenterY+BRANCH_LINE_WIDTH, centerX, centerY);
            });
        }
    }

    public Rectangle2D getElementBounds(@NotNull TreeNodeElement element,
                                        GameTreeElementSize elementSize,
                                        Camera camera) {
        var gridSize = elementSize.getGridSize();

        double width = gridSize.getWidth();
        double height = gridSize.getHeight();
        double x = element.getGridX() * width + camera.getOffsetX();
        double y = element.getGridY() * height + camera.getOffsetY();

        return new Rectangle2D(x, y, width, height);
    }

    /**
     * A set of listeners to receive canvas input events.
     */
    interface InputHandler {

        void mouseMoved(MouseEvent e);
        void mousePressed(MouseEvent e);
        void mouseClicked(MouseEvent e);
        void mouseDragged(MouseEvent e);
        void mouseExited(MouseEvent e);
        void mouseEntered(MouseEvent e);
        void mouseScrolled(ScrollEvent e);

        void keyPressed(KeyEvent e);

    }
}
