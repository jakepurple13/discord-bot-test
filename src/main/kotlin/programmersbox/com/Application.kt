package programmersbox.com

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import programmersbox.com.plugins.configureDatabases
import programmersbox.com.plugins.configureRouting
import programmersbox.com.plugins.configureSerialization
import reactor.core.publisher.Mono

const val CHANNEL_ID = "821697193097691180"

fun main(args: Array<String>) {
    val token = args.first()
    DiscordClient.create(token)
        .withGateway {
            fun printMessage(message: String) {
                mono {
                    it
                        .getChannelById(Snowflake.of(CHANNEL_ID))
                        .ofType(MessageChannel::class.java)
                        .flatMap { it.createMessage(message) }
                        .subscribe()
                }
            }

            mono {
                it
                    .getChannelById(Snowflake.of(CHANNEL_ID))
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
