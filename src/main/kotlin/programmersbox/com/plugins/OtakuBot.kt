package programmersbox.com.plugins

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

val Emerald = Color(0xFF2ecc71.toInt())
val DarkBlue = Color(0xffa9c7ff.toInt())

class OtakuBot(
    val settingsDb: SettingsDb,
    val databaseRepository: DatabaseRepository
) {
    private fun newScope() = CoroutineScope(Dispatchers.IO + Job())

    private val settings = settingsDb.getSettings()

    suspend fun printSettings() {
        settings.firstOrNull()?.let {
            println("Delay: ${it.delayInMillis}")
        }
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun setupOtakuChecking(
        textChannel: TextChannel,
        onCheck: suspend () -> Map<CheckTypes, List<ExtensionJsonObject>>
    ) {
        settings
            .map { it.delayInMillis }
            .distinctUntilChanged()
            .flatMapLatest {
                flow {
                    while (currentCoroutineContext().isActive) {
                        textChannel.createMessage {
                            content = "Starting Check..."
                            suppressNotifications = true
                        }
                        emit(onCheck())
                        delay(it)
                    }
                }
            }
            .onEach { list ->
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
            }
            .launchIn(newScope())
    }
}
