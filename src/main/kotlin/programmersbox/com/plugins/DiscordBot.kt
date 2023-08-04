package programmersbox.com.plugins

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import programmersbox.com.plugins.extensions.ChangeDelayExtension
import programmersbox.com.plugins.extensions.ShowFeaturesExtension

enum class CheckTypes { New, Update }

suspend fun DiscordBot(
    token: String,
    channelId: String,
    otakuBot: OtakuBot,
    onCheck: suspend () -> Map<CheckTypes, List<ExtensionJsonObject>>
) {
    val bot = ExtensibleBot(token) {
        presence { watching("/Reading Anime/Manga/Novels") }
        extensions {
            add { ShowFeaturesExtension(otakuBot.databaseRepository) }
            add { ChangeDelayExtension(otakuBot.settingsDb) }
            help {
                pingInReply = true
            }
        }
    }

    val c = bot.kordRef.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createSilentMessage("OtakuBot is booting up...Please wait...")

    otakuBot.printSettings()

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

    c?.createSilentMessage("OtakuBot is Online!")
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
                bot.stop()
            }
        }
    )

    bot.start()

    Thread.currentThread().join()
}

suspend fun TextChannel.createSilentMessage(content: String) {
    createMessage {
        this.content = content
        suppressNotifications = true
    }
}
