package programmersbox.com.plugins

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kord.common.entity.Snowflake
import programmersbox.com.plugins.extensions.games.BlackjackExtension
import programmersbox.com.plugins.extensions.games.PokerExtension
import programmersbox.com.plugins.extensions.games.ShowCashExtension

class Games : MutableMap<Snowflake, PlayerInfo> by mutableMapOf() {
    context (ExtensibleBotBuilder.ExtensionsBuilder)
    operator fun invoke() {
        add { BlackjackExtension(this) }
        add { PokerExtension(this) }
        add { ShowCashExtension(this) }
    }

    fun getOrPut(id: Snowflake) = getOrPut(id) { PlayerInfo() }
}

data class PlayerInfo(
    val money: Double = 500.0,
)