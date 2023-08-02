package programmersbox.com.plugins

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock

enum class CheckTypes { New, Update }

@OptIn(PrivilegedIntent::class)
suspend fun DiscordBot(
    token: String,
    channelId: String,
    otakuBot: OtakuBot,
    onCheck: suspend () -> Map<CheckTypes, List<ExtensionJsonObject>>
) {
    val kord = Kord(token)

    val c = kord.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createMessage("OtakuBot is booting up...Please wait...")

    otakuBot.printSettings()

    kord.on<MessageCreateEvent> {
        if (message.content == "!ping") message.channel.createMessage("Pong!")
    }

    kord.on<MessageCreateEvent> { otakuBot.onMessage() }

    /*runCatching {
        val image = Image.fromUrl(
            HttpClient(),
            "https://raw.githubusercontent.com/jakepurple13/OtakuWorld/develop/otakumanager/src/main/res/drawable/otakumanager_logo.png"
        )

        kord.editSelf {
            avatar = image
            username = "OtakuBot"
        }
    }*/

    c?.createMessage("OtakuBot is Online!")
    c?.let { otakuBot.setupOtakuChecking(it, onCheck) }
    c?.let { otakuBot.setupServerMessages(it) }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                c?.createMessage {
                    embed {
                        title = "Shutting Down for maintenance and updates..."
                        timestamp = Clock.System.now()
                        description = "Please wait while I go through some maintenance."
                        thumbnail {
                            url = "https://media.tenor.com/YTPLqiB6gLsAAAAC/sowwy-sorry.gif"
                        }
                        footer {
                            text = "I'll be reading some manga and novels and watching some anime."
                        }
                        color = Color(0xFFe74c3c.toInt())
                    }
                }
                kord.shutdown()
            }
        }
    )

    kord.login {
        presence { watching("/Reading Anime/Manga/Novels") }
        intents += Intent.MessageContent
    }

    Thread.currentThread().join()
}
