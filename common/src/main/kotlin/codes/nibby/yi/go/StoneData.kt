package codes.nibby.yi.go

/**
 * Represents a point on the game board
 */
class StoneData constructor(val x: Int, val y: Int, val stoneColor: GoStoneColor) {

    fun getIndex(boardWidth: Int) = x + y * boardWidth

    override fun equals(other: Any?): Boolean {
        if (other is StoneData) {
            return this.x == other.x && this.y == other.y && this.stoneColor == other.stoneColor
        }

        return false
    }

    override fun hashCode(): Int {
        return arrayOf(x, y, stoneColor).hashCode()
    }

    override fun toString(): String {
        return "($x, $y): $stoneColor"
    }
}