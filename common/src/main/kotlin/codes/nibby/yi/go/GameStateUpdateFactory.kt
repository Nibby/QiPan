package codes.nibby.yi.go

/**
 * Handles the creation of [GameStateUpdate] depending on the context.
 */
internal object GameStateUpdateFactory {

    fun createForProposedMove(primaryMove: StoneData, captures: HashSet<StoneData>, stateHash: Long): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.MOVE_PLAYED, primaryMove, captures, stateHash, HashSet())

    fun createForRootNode(emptyPositionStateHash: Long): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.ROOT, null, HashSet(), emptyPositionStateHash, HashSet())

    fun createForPassMove(currentPositionStateHash: Long): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.PASS, null, HashSet(), currentPositionStateHash, HashSet())

    fun createForResignationMove(currentPositionStateHash: Long): GameStateUpdate
        = GameStateUpdate(GameStateUpdate.Type.RESIGN, null, HashSet(), currentPositionStateHash, HashSet())

}