package codes.nibby.yi.go

import java.lang.IllegalStateException
import kotlin.random.Random

class ZobristHasher constructor(private val boardWidth: Int, boardHeight: Int) : StateHasher {

    private val intersectionCount = boardWidth * boardHeight
    private var hashLookup = Array<Long>(intersectionCount * GoStoneColor.values().size) { 0 }

    init {
        val usedNumbers: HashSet<Long> = HashSet()

        for (state in GoStoneColor.values().indices) {
            for (index in 0 until intersectionCount) {
                var uniqueStateHash = Random.nextLong()
                var retries = 0

                while (usedNumbers.contains(uniqueStateHash)) {
                    uniqueStateHash = Random.nextLong()
                    ++retries

                    // Don't expect this to happen at all, but it's part of the core code and
                    // we don't want any endless loops if something goes wrong.
                    if (retries > 10)
                        throw IllegalStateException("Too many collisions generating unique state hash")
                }

                val hashStateIndex = state.toByte() * intersectionCount + index
                hashLookup[hashStateIndex] = uniqueStateHash
                usedNumbers.add(uniqueStateHash)
            }
        }
    }

    override fun calculateStateHash(state: GoGameState): Long {
        val position = state.gamePosition
        var hash = getEmptyStateHash()

        position.intersectionState.forEachIndexed { stonePosition, stoneColor ->
            val intersectionHash = getHashValue(stoneColor, stonePosition)
            hash = hash xor intersectionHash
        }

        return hash
    }

    override fun calculateUpdateHash(currentStateHash: Long, stoneUpdates: Set<StoneData>): Long {
        var newHash = currentStateHash

        stoneUpdates.forEach { update ->
            val updateHash = getHashValue(update)
            newHash = newHash xor updateHash
        }

        return newHash
    }

    private fun getHashValue(data: StoneData): Long {
        return getHashValue(data.stoneColor, data.x + data.y * boardWidth)
    }

    private fun getHashValue(color: GoStoneColor, position: Int): Long {
        return hashLookup[color.index * intersectionCount + position]
    }
}