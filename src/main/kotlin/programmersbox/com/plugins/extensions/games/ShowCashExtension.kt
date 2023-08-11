@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions.games

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import programmersbox.com.plugins.Games

class ShowCashExtension(
    private val games: Games
) : Extension() {
    override val name: String = "mycash"

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "mycash"
            description = "Show how much money you have"
            action {
                respond {
                    content = games[user.id]
                        ?.let { "You have \$${it.money}" } ?: "You currently are not a player"
                }
            }
        }
    }

}