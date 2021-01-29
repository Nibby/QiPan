package yi.core.go

import yi.core.go.rules.ChineseRulesHandler
import yi.core.go.rules.GameRulesHandler
import yi.core.go.rules.NewZealandRulesHandler
import java.util.*

/**
 * A list of supported conventional rules in the game model. Each rule value has a
 * corresponding [GameRulesHandler] that contains all the relevant logic specific to that
 * ruleset.
 */
enum class StandardGameRules(val rulesHandler: GameRulesHandler) {

    CHINESE(ChineseRulesHandler()) {
        override fun isRuleset(rulesetName: String): Boolean = rulesetName.equals("chinese", ignoreCase = true)
    },

    NEW_ZEALAND(NewZealandRulesHandler()) {
        override fun isRuleset(rulesetName: String): Boolean {
            return rulesetName.equals("new zealand", ignoreCase = true)
                    || rulesetName.equals("nz", ignoreCase = true)
        }
    };

    /**
     * This method is often used to parse ruleset definition in text-form.
     *
     * @return true if the ruleset name is equal to this ruleset.
     */
    abstract fun isRuleset(rulesetName: String): Boolean

    companion object {
        /**
         * Converts a ruleset represented by a string to an enum value in this class.
         * Otherwise if no ruleset has this name, returns [Optional.empty].
         */
        fun parse(rulesetName: String): Optional<StandardGameRules> {
            return Arrays.stream(values())
                    .filter { ruleset -> ruleset.isRuleset(rulesetName) }
                    .findAny()
        }
    }
}