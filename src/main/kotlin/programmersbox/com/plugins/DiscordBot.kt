package programmersbox.com.plugins

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

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

    kord.on<MessageCreateEvent> {
        println(this)
        if (message.content == "!ping") message.channel.createMessage("Pong!")
    }

    //val image = Image.fromUrl(HttpClient(), "https://raw.githubusercontent.com/jakepurple13/OtakuWorld/develop/otakumanager/src/main/res/drawable/otakumanager_logo.png")

    /*kord.editSelf {
        avatar = image
        username = "OtakuBot"
    }*/

    c?.createMessage("OtakuBot is Online!")
    c?.let { otakuBot.setupOtakuChecking(it, onCheck) }
    c?.let { otakuBot.setupServerMessages(it) }

    kord.login {
        presence { watching("/Reading Anime/Manga") }
        intents += Intent.MessageContent
    }
}
