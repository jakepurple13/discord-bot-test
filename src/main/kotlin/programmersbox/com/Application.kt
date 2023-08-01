package programmersbox.com

import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.ktor.server.application.*
import programmersbox.com.plugins.configureDatabases
import programmersbox.com.plugins.configureRouting
import programmersbox.com.plugins.configureSerialization

@OptIn(PrivilegedIntent::class)
suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]
    val kord = Kord(token)

    val c = kord.getChannelOf<TextChannel>(dev.kord.common.entity.Snowflake(channelId))

    c?.createMessage("OtakuBot is Online!")

    kord.on<MessageCreateEvent> {
        if (message.content == "!ping") message.channel.createMessage("Pong!")
    }

    kord.login {
        presence { watching("/Reading Anime/Manga") }
        intents += Intent.MessageContent
    }

    /*mono {
        while (true) {
            val f = readln()
            println(f)
            printMessage(f)
            if (f == "stop") break
        }
    }*/
    //embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
}
