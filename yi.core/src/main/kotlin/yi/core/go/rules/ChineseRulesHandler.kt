package yi.core.go.rules

class ChineseRulesHandler : GameRulesHandler() {

    override fun getDefaultKomi(handicap: Int): Float {
        return when {
            handicap >= 1 -> 0.5f
            else -> 7.5f
        }
    }

    override fun allowSuicideMoves(): Boolean {
        return false
    }

    override fun getInternalName(): String {
        return "Chinese"
    }

}