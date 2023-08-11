package programmersbox.com.plugins

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.runBlocking
import programmersbox.com.plugins.database.GamesDatabase
import programmersbox.com.plugins.extensions.games.BlackjackExtension
import programmersbox.com.plugins.extensions.games.PokerExtension
import programmersbox.com.plugins.extensions.games.ShowCashExtension

class Games(
    private val gamesDatabase: GamesDatabase
) {
    context (ExtensibleBotBuilder.ExtensionsBuilder)
    operator fun invoke() {
        add { BlackjackExtension(this) }
        add { PokerExtension(this) }
        add { ShowCashExtension(this) }
    }

    operator fun get(snowflake: Snowflake) = runBlocking { gamesDatabase.read(snowflake) }
    operator fun set(snowflake: Snowflake, playerInfo: PlayerInfo) {
        runBlocking { update(snowflake, playerInfo) }
    }

    suspend fun getOrPut(id: Snowflake): PlayerInfo {
        val player = gamesDatabase.read(id)
        return if (player != null) {
            gamesDatabase.update(id, player)
            player
        } else {
            gamesDatabase.create(id)
        }
    }

    suspend fun update(playerInfo: PlayerInfo) {
        gamesDatabase.update(playerInfo.snowflake, playerInfo)
    }

    suspend fun update(snowflake: Snowflake, playerInfo: PlayerInfo) {
        gamesDatabase.update(snowflake, playerInfo)
    }
}

data class PlayerInfo(
    val money: Double = 500.0,
    val snowflake: Snowflake
)