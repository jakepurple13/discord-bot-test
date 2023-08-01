package programmersbox.com

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.MessageCreateSpec
import io.ktor.server.application.*
import kotlinx.coroutines.reactor.mono
import programmersbox.com.plugins.configureDatabases
import programmersbox.com.plugins.configureRouting
import programmersbox.com.plugins.configureSerialization

fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]
    DiscordClient.create(token)
        .withGateway {
            fun printMessage(message: String) {
                mono {
                    it
                        .getChannelById(Snowflake.of(channelId))
                        .ofType(MessageChannel::class.java)
                        .flatMap { it.createMessage(message) }
                        .subscribe()
                }
            }

            mono {
                it
                    .getChannelById(Snowflake.of(channelId))
                    .ofType(MessageChannel::class.java)
                    .flatMap {
                        it.createMessage(
                            MessageCreateSpec.builder()
                                .content("OtakuBot is Online!")
                                .build()
                        )
                    }
                    .subscribe()
            }

            /*mono {
                while (true) {
                    val f = readln()
                    println(f)
                    printMessage(f)
                    if (f == "stop") break
                }
            }*/
        }.block()
    //embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
}
