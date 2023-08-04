@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.flow.first
import programmersbox.com.plugins.SettingsDb
import kotlin.time.Duration.Companion.minutes

class ChangeDelayExtension(
    private val settingsDb: SettingsDb
) : Extension() {
    override val name: String = "setdelay"

    override suspend fun setup() {
        publicSlashCommand(::ChangeDelayArgs) {
            name = "setdelay"
            description = "Change the update check delay"
            check {
                isNotBot()
            }

            action {
                runCatching {
                    if (member?.asMember()?.isOwner() == false) {
                        respond { content = "Must be owner to change the delay" }
                        error("Only Owner can change the delay!")
                    }

                    val amount = arguments.delay
                    runCatching { amount.toLong() }
                        .onSuccess { settingsDb.updateSettings { delayInMillis = it } }
                        .onFailure {
                            if (amount == "reset")
                                settingsDb.updateSettings { delayInMillis = 60.minutes.inWholeMilliseconds }
                        }

                    respond {
                        content = "Changing check delay to ${settingsDb.getSettings().first().delayInMillis}ms"
                    }
                }
            }
        }
    }

    inner class ChangeDelayArgs : Arguments() {
        val delay by defaultingString {
            name = "delay"
            description = "Choose"
            defaultValue = "reset"
        }
    }
}