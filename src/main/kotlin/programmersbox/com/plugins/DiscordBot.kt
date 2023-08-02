package programmersbox.com.plugins

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
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

    c?.createSilentMessage("OtakuBot is booting up...Please wait...")

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

    /*kord.createGlobalChatInputCommand(
        "showsourceurl",
        "Get the url to download a source"
    ) {
        string("fromfeature", "Choose the feature type to narrow down the search") {
            choice("anime", "anime")
            choice("manga", "manga")
            choice("novel", "novel")
            required = true
        }
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferEphemeralResponse()
        val command = interaction.command
        val feature = command.strings["fromfeature"]!!
        val source = otakuBot.databaseRepository.loadFeaturesFromDb(feature)
        response.respond {
            content = "Here is the url to download the apk!"
            actionRow {
                stringSelect("apk") {
                    source.forEach { option(it.name, it.name) }
                }

                linkButton("source?.apkUrl.orEmpty()") {
                    label = "Source Url!"
                }
            }
        }
    }*/

    kord.createGlobalChatInputCommand(
        "showfeatures",
        "Show all the sources of a chosen feature"
    ) {
        string("feature", "Feature Type") {
            choice("anime", "anime")
            choice("manga", "manga")
            choice("novel", "novel")
            required = true
        }
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferEphemeralResponse()
        val command = interaction.command
        val feature = command.strings["feature"]!!
        response.respond { otakuBot.showFeatureTypes(feature) }
    }

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
                kord.shutdown()
            }
        }
    )

    kord.login {
        presence { watching("/Reading Anime/Manga/Novels") }
        intents += Intents(Intent.MessageContent, Intent.DirectMessages, Intent.GuildMessages)
    }

    Thread.currentThread().join()
}

suspend fun TextChannel.createSilentMessage(content: String) {
    createMessage {
        this.content = content
        suppressNotifications = true
    }
}
