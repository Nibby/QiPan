package yi.core.go.rules

import yi.core.go.GameModelInfo
import yi.core.go.StoneColor

abstract class GameRulesHandler {

    /**
     * @param nextMoveNumber Move number with 0 being the first move.
     * @param handicap Number of handicap stones in this game.
     *
     * @return Color of the player expected to play at that given move number.
     */
    fun getStoneColorForTurn(nextMoveNumber: Int, handicap: Boolean): StoneColor {
        return if (handicap) {
            if (nextMoveNumber % 2 == 0) StoneColor.WHITE else StoneColor.BLACK
        } else {
            if (nextMoveNumber % 2 == 0) StoneColor.BLACK else StoneColor.WHITE
        }
    }

    /**
     * Returns the default komi value to be used for the game if one isn't explicitly
     * specified in [GameModelInfo.getKomi].
     *
     * @param handicap Number of handicaps used in this game. The default case
     * should assume no handicaps.
     */
    abstract fun getDefaultKomi(handicap: Int = 0): Float

    /**
     * Returns true if the ruleset allows suicide moves to be played.
     */
    abstract fun allowSuicideMoves(): Boolean

    /**
     * Returns a unique name used internally by the application. The name should conform
     * to standard SGF conventions since it will be exported as such.
     */
    abstract fun getInternalName(): String

}