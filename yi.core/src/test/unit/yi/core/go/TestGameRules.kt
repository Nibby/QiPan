package yi.core.go

import yi.core.go.rules.GameRulesHandler

class TestGameRules {
    class TestingGameRulesNoSuicide : GameRulesHandler() {
        override fun getDefaultKomi(handicap: Int): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = false
        override fun getInternalName(): String {
            return "TestingRulesNoSuicide"
        }
    }

    class TestingGameRulesSuicideAllowed : GameRulesHandler() {
        override fun getDefaultKomi(handicap: Int): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = true
        override fun getInternalName(): String {
            return "TestingRulesSuicide"
        }
    }
}