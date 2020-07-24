package yi.core.go

import yi.core.go.rules.GoGameRulesHandler

class TestGameRules {
    class TestingGameRulesNoSuicide : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = false
    }

    class TestingGameRulesSuicideAllowed : GoGameRulesHandler() {
        override fun getKomi(): Float = 6.5F
        override fun allowSuicideMoves(): Boolean = true
    }
}