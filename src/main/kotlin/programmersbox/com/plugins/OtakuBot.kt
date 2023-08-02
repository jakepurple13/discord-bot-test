package programmersbox.com.plugins

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

private val Emerald = Color(0xFF2ecc71.toInt())
private val DarkBlue = Color(0xffa9c7ff.toInt())

class OtakuBot {
    private fun newScope() = CoroutineScope(Dispatchers.IO + Job())

    fun setupServerMessages(
        textChannel: TextChannel
    ) {
        newScope().launch {
            while (coroutineContext.isActive) {
                val f = readln()
                println(f)
                textChannel.createMessage(f)
                if (f == "stop") break
            }
        }
    }

    fun setupOtakuChecking(
        textChannel: TextChannel,
        onCheck: suspend () -> Map<CheckTypes, List<ExtensionJsonObject>>
    ) {
        newScope().launch {
            while (coroutineContext.isActive) {
                val list = onCheck()
                val new = list[CheckTypes.New].orEmpty()
                val update = list[CheckTypes.Update].orEmpty()
                if (new.isNotEmpty() || update.isNotEmpty()) {
                    textChannel.createMessage {
                        content = "There's some updates!"

                        if (new.isNotEmpty()) {
                            embed {
                                title = "New"
                                color = Emerald
                                new.forEach { field(it.name) { it.version } }
                            }
                        }

                        if (update.isNotEmpty()) {
                            embed {
                                title = "Update"
                                color = DarkBlue
                                update.forEach { field(it.name) { it.version } }
                            }
                        }
                    }
                }
                delay(60.minutes)
            }
        }
    }
}
