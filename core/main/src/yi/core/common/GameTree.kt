package yi.core.common

open class GameTree<NodeData> constructor(rootNode: GameNode<NodeData>) {

    internal var rootNode: GameNode<NodeData> = rootNode

    init {
        this.rootNode.markAsRoot()
    }

    constructor() : this(GameNode())

    /**
     * Appends a child node to a parent node in the tree.
     *
     * @throws IllegalArgumentException if parent is part of another tree, child already has a parent, child is a root node, or parent == child
     */
    fun appendNode(parent: GameNode<NodeData>, child: GameNode<NodeData>) {
        if (!isDescendant(parent))
            throw IllegalArgumentException("Parent node is not descendent of this move tree")
        if (child.parent != null)
            throw IllegalArgumentException("Child node already has an active parent")
        if (child.markedAsRoot)
            throw IllegalArgumentException("Child node is a root node")
        if (parent == child)
            throw IllegalArgumentException("A node cannot be a parent of itself")

        parent.children.add(child)
        child.parent = parent

        child.root = rootNode
        child.position = parent.position + 1
    }

    /**
     * Convenience method that is equivalent to [appendNode] with [rootNode] as parent. In other words,
     * appends the child node as a direct descendant of the tree root.
     *
     * @throws IllegalArgumentException see kotlin docs for [appendNode]
     */
    fun appendNode(child: GameNode<NodeData>) {
        appendNode(rootNode, child)
    }

    /**
     * Removes the specified node from the game tree, erasing association with its parent and children.
     *
     * Performing this operation will divide the tree if the node is not a leaf. To remove all children and their
     * descendants, use [removeNodeSubtree] to completely remove all descendants following the removed node.
     *
     * @param node The node to be removed from the game tree
     * @throws IllegalArgumentException If the node to be removed does not belong to this tree
     */
    fun removeNode(node: GameNode<NodeData>) {
        if (!isDescendant(node))
            throw IllegalArgumentException("Cannot remove a node that is not part of this move tree")

        node.parent?.children?.remove(node)
        node.children.forEach { child -> child.parent = null }
        node.root = null
    }

    /**
     * Removes the specified node from the game tree, erasing association with its parent and children, as well as
     * removing all descendants associated with the node. This will erase the entire subtree starting from the given
     * node.
     *
     * @param node The node and its subtree to be removed from the game tree
     * @throws IllegalArgumentException If the node to be removed does not belong to this tree
     */
    @Suppress("UNCHECKED_CAST") // Should be safe to cast as NodeType inherits from GameNode
    fun removeNodeSubtree(node: GameNode<NodeData>) {
        if (!isDescendant(node))
            throw IllegalArgumentException("Cannot remove a node that is not part of this move tree")

        node.parent?.children?.remove(node)
        node.children.forEach { child -> child.parent = null; removeNodeSubtree(child); }
        node.children.clear()
    }

    /**
     * Check whether the given node is a descendent of this tree root.
     *
     * @param node The node to check
     * @return true if the node is belongs to this tree
     */
    fun isDescendant(node: GameNode<NodeData>): Boolean {
        return node.root == rootNode
    }
}