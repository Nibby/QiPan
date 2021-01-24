package yi.core.go

import yi.core.go.docformat.FileFormat
import yi.core.go.editor.GameModelEditor
import yi.core.go.rules.GameRulesHandler
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList

/**
 * Representing one game of Go. The model tracks
 *
 * Unlike the [GameTree], methods in this model should be more domain-specific.
 *
 * //TODO: Explain the GameStateUpdate system
 * //TODO: Explain organisation internally, esp. how state is calculated and retrieved
 * //TODO: Explain how to submit moves
 */
class GameModel(val boardWidth: Int,
                val boardHeight: Int,
                val rules: GameRulesHandler,
                val stateHasher: GameStateHasher) {

    internal val gameTree = GameTree(stateHasher.computeEmptyPositionHash(boardWidth, boardHeight))
    var currentNode = gameTree.rootNode
        set(value) {
            if (!gameTree.isDescendant(value))
                throw IllegalArgumentException("Node does not belong to the model game tree")

            field = value
            internalCurrentNodeUpdate(value)
            currentNodeChangeEventHook.fireEvent(NodeEvent(value))
        }

    internal var playedMoveHistory: List<GameNode> = LinkedList()
    private var stateHashHistory: List<Long> = LinkedList()
    private var stateCache = WeakHashMap<Long, GameState>()
    val editor = GameModelEditor(this)

    var lastSavePath: Path? = null
    var lastSaveFormat: FileFormat? = null
    var isModified = false
    val info = GameModelInfo()

    init {
        if (boardWidth < 1 || boardHeight < 1) {
            throw IllegalArgumentException("Invalid board dimensions: $boardWidth x $boardHeight")
        }

        info.setKomi(rules.getDefaultKomi())
    }

    constructor(boardWidth: Int, boardHeight: Int, rulesHandler: GameRulesHandler)
            : this(boardWidth, boardHeight, rulesHandler, ZobristHasher(boardWidth, boardHeight))

    constructor(boardWidth: Int, boardHeight: Int, rulesStandard: StandardGameRules)
            : this(boardWidth, boardHeight, rulesStandard.rulesHandler, ZobristHasher(boardWidth, boardHeight))


    private fun internalCurrentNodeUpdate(currentNode: GameNode) {
        val nodeHistory = currentNode.getMoveHistory()
        this.playedMoveHistory = nodeHistory.filter {
            it.delta.type == GameNodeType.MOVE_PLAYED
                || it.delta.type == GameNodeType.PASS
        }
        if (nodeHistory.size > 1) {
            // Only count non-root and primary move updates for unique state
            val uniqueStateHistory = nodeHistory.subList(1, nodeHistory.size)
                    .filter { node -> node.getType() == GameNodeType.MOVE_PLAYED }
            this.stateHashHistory = uniqueStateHistory.map { node -> node.getStateHash() }
        } else {
            this.stateHashHistory = ArrayList()
        }
    }

    /**
     * Adjusts the root node of the game model. This is commonly used for loading a game
     * model from file, where the root node may contain additional metadata.
     *
     * This operation should only be performed once immediately after creating the game
     * model. If the current root node has descendants, the method will throw an
     * [IllegalStateException].
     */
    @Suppress("FunctionName") // Using underscore to denote non-standard operation
    internal fun _setRootNode(rootNode: GameNode) {
        if (gameTree.rootNode.getChildNodes().isNotEmpty()) {
            throw IllegalStateException("Cannot set root node after it has descendants. " +
                    "This operation should be performed before adding any additional nodes.")
        }

        val updateCurrentMove = currentNode == gameTree.rootNode
        gameTree.rootNode = rootNode
        if (updateCurrentMove) {
            currentNode = rootNode
        }
    }

    /**
     * Returns a handler to play a series of moves in succession. This is the recommended
     * approach when submitting multiple moves to the game model with the assumption that
     * each move must be submitted successfully.
     *
     * @see MoveSequence
     */
    fun beginMoveSequence(): MoveSequence {
        return MoveSequence(this)
    }

    /**
     * Equivalent to invoking [getGameState] at [currentNode].
     *
     * @return The [GameState] at the current position.
     */
    fun getCurrentGameState(): GameState {
        return getGameState(currentNode)
    }

    /**
     * The game state is dynamically calculated if it does not exist, otherwise it will be
     * cached for a period of time for performance.
     *
     * @return The [GameState] at a given node position.
     */
    fun getGameState(gameNode: GameNode): GameState {
        if (!gameTree.isDescendant(gameNode))
            throw IllegalArgumentException("Game node is not part of this move tree")

        if (gameNode.getType() == GameNodeType.MOVE_PLAYED) {
            this.stateCache[gameNode.getStateHash()]?.let {
                return it
            }
        }

        // Only perform state resolution if we don't have a cached position
        val positionState = GamePosition(boardWidth, boardHeight)

        var prisonersWhite = 0
        var prisonersBlack = 0

        val annotations: Collection<Annotation>

        // Build the board state by traversing the history and apply the delta from root up to gameNode
        val pathToRoot = gameNode.getMoveHistory()

        pathToRoot.forEach { node ->
            val captures = node.getCapturesThisTurn()

            positionState.apply(node.delta)
            prisonersWhite += captures.filter { capture -> capture.color == StoneColor.BLACK }.count()
            prisonersBlack += captures.filter { capture -> capture.color == StoneColor.WHITE }.count()
        }

        annotations = pathToRoot[pathToRoot.lastIndex].getAnnotations()

        val gameState = GameState(this, positionState, gameNode, prisonersWhite,
                prisonersBlack, annotations)
        this.stateCache[gameNode.getStateHash()] = gameState

        return gameState
    }

    /**
     *
     * @return Set of all annotations on the current node.
     */
    fun getAnnotationsOnCurrentNode(): Collection<Annotation> {
        return getAnnotations(currentNode)
    }

    /**
     *
     * @return Set of all annotations on the specified node.
     */
    fun getAnnotations(node: GameNode): Collection<Annotation> {
        return node.getAnnotations()
    }

    /**
     *
     * @return The [StoneColor] for the stone that will be played on the next turn.
     */
    fun getNextTurnStoneColor(): StoneColor {
        return rules.getStoneColorForTurn(playedMoveHistory.size, info.getHandicapCount() > 0)
    }

    /**
     *
     * @return Total number of intersections on this game board.
     */
    fun getIntersectionCount() = boardWidth * boardHeight

    /**
     * Returns a copy of the state hash history that led up to the current state.
     */
    fun getStateHashHistory(): List<Long> {
        return ArrayList(stateHashHistory)
    }

    /**
     * The move number includes stone edit nodes as well as played moves, pass and
     * resignations.
     *
     * @return An integer representing the current move number, with zero being the root
     * node.
     */
    fun getCurrentMoveNumber(): Int {
        return currentNode.moveNumber
    }

    /**
     * The move number includes stone edit nodes as well as played moves, pass and
     * resignations.
     *
     * @return An integer representing the next move number, with zero being the root node.
     */
    fun getNextMoveNumber(): Int {
        return getCurrentMoveNumber() + 1
    }

    /**
     * @return The move preceding [currentNode], or null if there is no previous move.
     */
    fun getPreviousNode(): GameNode? {
        return currentNode.parent
    }

    /**
     * Sets the current move to the previous move if it is not the root.
     * Otherwise, do nothing.
     *
     * @return The move preceding [currentNode], or null if there is no previous move.
     */
    fun toPreviousNode(): GameNode {
        return toPreviousNode(1)
    }

    /**
     * Go to the move that is n moves before the current node. If that move does not exist
     * because the top of the tree (root) is reached, the method fails gracefully by
     * setting the current position at the root node.
     *
     * @return The node this method arrived at (which will be the new current node).
     */
    fun toPreviousNode(steps: Int): GameNode {
        var newPosition = currentNode

        for (i in 0 until steps) {
            if (newPosition.parent != null) {
                newPosition = newPosition.parent!!
            } else {
                break
            }
        }
        currentNode = newPosition

        return newPosition
    }

    /**
     * Sets the current move to the next move if it is not the last move in this variation.
     * Otherwise, do nothing.
     *
     * @return The next move if it is available, otherwise null.
     */
    fun toNextNode(): GameNode {
        return toNextNode(1)
    }

    /**
     * Go to the move that is n moves after the current node. If that move does not exist
     * because the branch ended prematurely, the current move will be set to the last move
     * in this branch.
     *
     * @return The node that this method arrived at (which will be the new current node).
     */
    fun toNextNode(steps: Int): GameNode {
        var newPosition = currentNode

        for (i in 0 until steps) {
            if (newPosition.children.isNotEmpty()) {
                newPosition = newPosition.children[0]
            } else {
                break
            }
        }
        currentNode = newPosition

        return newPosition
    }

    /**
     *
     * @return The root node of the game tree.
     */
    fun getRootNode(): GameNode {
        return gameTree.rootNode
    }

    // -- Observable property declarations

    /**
     * Emitter for current node change events. In other words, an event will be fired from
     * this event hook if a new game node becomes the current node.
     */
    fun onCurrentNodeChange(): NodeEventHook = currentNodeChangeEventHook
    private val currentNodeChangeEventHook = NodeEventHook()

    /**
     * Emitter for node addition events. This event is always emitted before
     * [onCurrentNodeChange], clients should not use [currentNode] in the listener
     * code block because the information is almost always out of date.
     *
     * To retrieve the correct current node, subscribe to [onCurrentNodeChange] instead.
     */
    fun onNodeAdd(): NodeEventHook = nodeAdditionEventHook
    private val nodeAdditionEventHook = NodeEventHook()

    /**
     * Emitter for node deletion events.
     */
    fun onNodeRemove(): NodeEventHook = nodeDeletionEventHook
    private val nodeDeletionEventHook = NodeEventHook()

    /**
     * Emitter for current node data update events.
     *
     * This event does not represent a change in the current node, rather it is the change
     * in the data on the current node. To determine whether the current node itself has
     * changed, subscribe to [onCurrentNodeChange] instead.
     */
    // Events in this hook doesn't need to be fired explicitly.
    fun onCurrentNodeDataUpdate(): NodeEventHook = currentNodeDataUpdateEventHook
    private val currentNodeDataUpdateEventHook = NodeEventHook()

    /**
     * Emitter for node data update events that may or may not be the current move.
     *
     * To subscribe to node data updates for only the current game move, use
     * [onCurrentNodeDataUpdate].
     */
    fun onNodeDataUpdate(): NodeEventHook = nodeDataUpdateEventHook
    private val nodeDataUpdateEventHook = NodeEventHook()

    /**
     * Destroys the game model by removing all data from the game tree and clearing all
     * event hook listeners.
     */
    fun dispose() {
        onCurrentNodeChange().removeAllListeners()
        onNodeAdd().removeAllListeners()
        onNodeRemove().removeAllListeners()
        onCurrentNodeDataUpdate().removeAllListeners()
        onNodeDataUpdate().removeAllListeners()

        editor.removeNodeSubtree(getRootNode())
        info.dispose()
    }

    // This init block has to be done last because the fields are initialized in order.
    init {
        val currentNodeDataUpdateEventEmitter = object : EventListener<NodeEvent> {
            override fun onEvent(event: NodeEvent) {
                if (event.node == currentNode) {
                    onCurrentNodeDataUpdate().fireEvent(event)
                }
            }
        }

        onNodeDataUpdate().addListener(currentNodeDataUpdateEventEmitter)
    }
}