package yi.core.go

/**
 * A manager class that enables a sequence of moves to be played while checking each move is played successfully.
 */
class GoMoveSequence constructor(private val game: GoGameModel) {

    /**
     * Synonymous to [GoGameModel.playMove], but introduces an additional state check to verify the move is submitted successfully.
     * Otherwise a [GoGameRulesException] is thrown to interrupt the sequence flow.
     */
    fun playMove(x: Int, y:Int): GoMoveSequence {
        val result = game.playMove(x, y)
        if (result.validationResult != GoMoveValidationResult.OK) {
            throw GoGameRulesException(result.validationResult, "Cannot play move ($x, $y) at node position ${game.getCurrentMove().getDistanceToRoot()}: ${result.validationResult}")
        }
        return this
    }

    /**
     * Synonymous to [GoGameModel.addAnnotationOnThisMove]
     *
     * @see [GoGameModel.addAnnotationOnThisMove]
     */
    fun annotate(annotation: GoAnnotation): GoMoveSequence {
        game.addAnnotationOnThisMove(annotation)
        return this
    }

    /**
     * Synonymous to [GoGameModel.playPass].
     *
     * @see [GoGameModel.playPass]
     */
    fun pass(): GoMoveSequence {
        game.playPass()
        return this
    }

    /**
     * Synonymous to [GoGameModel.playResign].
     *
     * @see [GoGameModel.playResign]
     */
    fun resign(): GoMoveSequence {
        game.playResign()
        return this
    }
}