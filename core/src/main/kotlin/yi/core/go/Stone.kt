package yi.core.go

import java.util.*

/**
 * A tuple that describes the state of a position.
 */
class Stone constructor(val x: Int, val y: Int, val stoneColor: StoneColor) {

    /**
     * @return index-based position representation of this stone.
     */
    fun getPosition(boardWidth: Int) = x + y * boardWidth

    override fun equals(other: Any?): Boolean {
        if (other is Stone) {
            return this.x == other.x && this.y == other.y && this.stoneColor == other.stoneColor
        }

        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y, stoneColor)
    }

    override fun toString(): String {
        return "($x, $y): $stoneColor"
    }
}