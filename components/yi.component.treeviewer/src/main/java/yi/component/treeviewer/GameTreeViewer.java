package yi.component.treeviewer;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.NullableProperty;
import yi.component.shared.NullablePropertyListener;
import yi.component.shared.component.CanvasContainer;
import yi.component.shared.component.YiComponent;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.NodeEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements YiComponent {

    private final CanvasContainer canvasContainer;
    private GameTreeViewerSettings settings = GameTreeViewerSettings.getDefault();
    private final GameTreeCanvas canvas;
    private final Camera camera;

    private GameModel gameModel;
    private final GameTreeStructure treeStructure = new GameTreeStructure();
    private final GameTreeElementSize elementSize;
    private final NullableProperty<GameNode> highlightedNode = new NullableProperty<>(null);

    public GameTreeViewer() {
        canvas = new GameTreeCanvas();
        canvas.addInputHandler(new CanvasInputHandler());

        canvasContainer = new CanvasContainer(canvas);
        elementSize = new GameTreeElementSize();
        camera = new Camera(canvasContainer.getWidth(), canvasContainer.getHeight());

        canvasContainer.addSizeUpdateListener(newSize -> {
            camera.setViewportSize(newSize.getWidth(), newSize.getHeight());
            render();
        });

        camera.addOffsetChangeListener(this::render);
    }

    public void setSettings(GameTreeViewerSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    private void updateCameraAndRender(GameNode nodeToCenter) {
        treeStructure.getTreeNodeElementForNode(nodeToCenter)
                .ifPresent(treeElement -> camera.setCenterElementWithAnimation(treeElement, elementSize.getGridSize()));

        render();
    }

    private void render() {
        if (gameModel != null && treeStructure != null) {
            var elements = getVisibleElementsInViewport();
            var currentNode = gameModel.getCurrentNode();
            var previewNode = treeStructure.getHighlightedNodePath();

            canvas.render(settings, camera, elements, currentNode, previewNode, elementSize);
        }
    }

    private List<TreeNodeElement> getVisibleElementsInViewport() {
        var gridWidth = elementSize.getGridSize().getWidth();
        var gridHeight = elementSize.getGridSize().getHeight();
        var offsetY = -camera.getOffsetY();

        // Some branches may be very far from their parent, in this case we still want to render it
        var startX = 0;
        var startY = (int) Math.floor(offsetY / gridHeight) - 1;
        var endX = startX + (int) Math.ceil(canvas.getWidth() / gridWidth) + 3;
        var endY = startY + (int) Math.ceil(canvas.getHeight() / gridHeight) + 3;

        return treeStructure.getNodeElementsWithinVerticalRegion(startX, startY, endX, endY);
    }

    private final EventListener<NodeEvent> treeStructureChangeListener = (event) -> {
        treeStructure.reconstruct();
        render();
    };

    private final EventListener<NodeEvent> currentMoveChangeListener = (event) -> updateCameraAndRender(event.getNode());
    private final EventListener<NodeEvent> currentMoveDataChangeListener = (event) -> render();

    public void setGameModel(@NotNull GameModel model) {
        boolean panToNewNode = false;

        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeChange().removeListener(currentMoveChangeListener);
            this.gameModel.onCurrentNodeDataUpdate().removeListener(currentMoveDataChangeListener);
            this.gameModel.onNodeAdd().removeListener(treeStructureChangeListener);
            this.gameModel.onNodeRemove().removeListener(treeStructureChangeListener);
            panToNewNode = true;
        }

        this.gameModel = model;
        this.treeStructure.setGameModel(model);

        var currentNode = this.gameModel.getCurrentNode();
        TreeNodeElement currentNodeElement = this.treeStructure.getTreeNodeElementForNode(currentNode).orElseThrow();
        if (panToNewNode) {
            this.camera.setCenterElementWithAnimation(currentNodeElement, elementSize.getGridSize());
        } else {
            this.camera.setCenterElementImmediately(currentNodeElement, elementSize.getGridSize());
        }

        this.gameModel.onCurrentNodeChange().addListener(currentMoveChangeListener);
        this.gameModel.onCurrentNodeDataUpdate().addListener(currentMoveDataChangeListener);
        this.gameModel.onNodeAdd().addListener(treeStructureChangeListener);
        this.gameModel.onNodeRemove().addListener(treeStructureChangeListener);

        updateCameraAndRender(model.getCurrentNode());
    }

    /**
     * Subscribes to current highlighted node update events. Highlighted node may be
     * {@code null} if none is highlighted.
     *
     * @param listener New listener to add.
     */
    public void addPreviewNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        highlightedNode.addListener(listener);
    }

    public void removePreviewNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        highlightedNode.removeListener(listener);
    }

    @Override
    public Parent getComponent() {
        return canvasContainer;
    }

    public @Nullable GameNode getHighlightedNode() {
        return treeStructure.getHighlightedNodePath();
    }

    public void setHighlightedNodePathAndCenterCamera(@Nullable GameNode endPoint) {
        setHighlightedNodePath(endPoint);
        updateCameraAndRender(endPoint != null ? endPoint : this.gameModel.getCurrentNode());
    }

    public void setHighlightedNodePath(GameNode endPoint) {
        treeStructure.setHighlightedNodePath(endPoint);
    }

    /**
     * Retrieves the render position of the {@link GameNode} on the game tree.
     *
     * @param node Node to retrieve bounds for.
     * @return The bounds of the {@link TreeElement} as it is rendered on the tree canvas
     * for the given node. Or {@link Optional#empty()} if the tree structure does not
     * contain the requested node.
     */
    protected final Optional<Rectangle2D> getElementBoundsForNode(GameNode node) {
        if (treeStructure == null) {
            return Optional.empty();
        }
        var element = new AtomicReference<TreeNodeElement>(null);
        treeStructure.getTreeNodeElementForNode(node).ifPresent(element::set);
        var value = element.get();
        if (value != null) {
            var bounds = canvas.getElementBounds(value, elementSize, camera);
            return Optional.of(bounds);
        }
        return Optional.empty();
    }

    protected final Camera getCamera() {
        return camera;
    }

    protected GameTreeStructure getTreeStructure() {
        return treeStructure;
    }

    /*
        Put this here to access all the required fields without introducing excessive coupling to the
        canvas component itself.
     */
    final class CanvasInputHandler implements GameTreeCanvas.InputHandler {

        private double dragStartX = 0;
        private double dragStartY = 0;
        private boolean isDragging = false;

        private GameNode lastFiredHighlightedNodeValue = null;

        @Override
        public void mouseMoved(MouseEvent e) {
            if (treeStructure != null) {
                int[] gridPosition = getGridPosition(e);
                int x = gridPosition[0];
                int y = gridPosition[1];

                boolean hasHighlight = treeStructure.setHighlightedGrid(x, y);

                if (hasHighlight) {
                    canvas.setCursor(Cursor.HAND);
                } else {
                    canvas.setCursor(Cursor.OPEN_HAND);
                }

                maybeFireHighlightedNodeChangeEvent(hasHighlight);
                render();
            }
        }

        private void maybeFireHighlightedNodeChangeEvent(boolean hasHighlight) {
            GameNode newValue = treeStructure.getHighlightedNodePath();
            if (hasHighlight) {
                if (newValue != gameModel.getCurrentNode()) {
                    if (lastFiredHighlightedNodeValue == null
                            || !lastFiredHighlightedNodeValue.equals(newValue)) {
                        highlightedNode.set(newValue);
                    }
                    lastFiredHighlightedNodeValue = newValue;
                } else {
                    highlightedNode.set(null);
                    lastFiredHighlightedNodeValue = null;
                }
            } else if (lastFiredHighlightedNodeValue != null) {
                lastFiredHighlightedNodeValue = null;
                highlightedNode.set(null);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragStartX = e.getX();
            dragStartY = e.getY();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            getComponent().requestFocus();

            if (treeStructure != null && !isDragging) {
                int[] gridPosition = getGridPosition(e);
                int x = gridPosition[0];
                int y = gridPosition[1];

                treeStructure.getNodeElement(x, y).ifPresent(element -> {
                    var selectedNode = element.getNode();
                    gameModel.setCurrentNode(selectedNode);
                    highlightedNode.set(null);
                });
            }

            isDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (treeStructure != null) {
                isDragging = true;
                canvas.setCursor(Cursor.CLOSED_HAND);

                double xDiff = e.getX() - dragStartX;
                double yDiff = e.getY() - dragStartY;
                dragStartX = e.getX();
                dragStartY = e.getY();

                double offsetX = camera.getOffsetX();
                double offsetY = camera.getOffsetY();

                setBoundedOffset(offsetX + xDiff, offsetY + yDiff);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setHighlightedNodePath(null);
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        private void setBoundedOffset(double offsetX, double offsetY) {
            double viewWidth = canvas.getWidth();
            double viewHeight = canvas.getHeight();

            double leftBound = viewWidth / 4 * 3;
            double rightBound = leftBound - viewWidth / 2
                    - (treeStructure.getFurthestHorizontalNode()+1) * elementSize.getGridSize().getWidth();
            double topBound = viewHeight / 4 * 3;
            double bottomBound = topBound - viewHeight / 2
                    - (treeStructure.getFurthestVerticalNode()) * elementSize.getGridSize().getHeight();

            double boundOffsetX = offsetX;
            double boundOffsetY = offsetY;

            if (boundOffsetX > leftBound) {
                boundOffsetX = leftBound;
            }
            if (boundOffsetX < rightBound) {
                boundOffsetX = rightBound;
            }

            if (boundOffsetY > topBound) {
                boundOffsetY = topBound;
            }
            if (boundOffsetY < bottomBound) {
                boundOffsetY = bottomBound;
            }

            camera.setOffset(boundOffsetX, boundOffsetY);
        }

        @Override
        public void mouseScrolled(ScrollEvent e) {
            if (treeStructure != null) {
                // TODO: Scrolling behaviour adjusts view offset rather than game
                //       state. Potentially make this adjustable in preferences so
                //       that those who are migrating from another app can feel at home.
                double deltaX = e.getDeltaX();
                double deltaY = e.getDeltaY();

                double offsetX = camera.getOffsetX();
                double offsetY = camera.getOffsetY();

                camera.setOffset(offsetX + deltaX, offsetY + deltaY);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            render();
        }

        private int[] getGridPosition(MouseEvent e) {
            double gridWidth = elementSize.getGridSize().getWidth();
            double gridHeight = elementSize.getGridSize().getHeight();

            int gridX = (int) Math.round(((e.getX() - gridWidth / 2) - camera.getOffsetX()) / gridWidth);
            int gridY = (int) Math.round(((e.getY() - gridHeight / 2) - camera.getOffsetY()) / gridHeight);

            return new int[] { gridX, gridY };
        }
    }
}
