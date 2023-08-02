package programmersbox.com.plugins

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val Emerald = Color(0xFF2ecc71.toInt())
private val DarkBlue = Color(0xffa9c7ff.toInt())

class OtakuBot(
    private val settingsDb: SettingsDb,
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

    context (MessageCreateEvent)
    suspend fun onMessage() = runCatching {
        val messageInfo = message.content.split(" ")
        when (messageInfo.firstOrNull()) {
            "!setDelay" -> setDelay(messageInfo)
            else -> {}
        }
    }

    context (InteractionResponseModifyBuilder)
    suspend fun showFeatureTypes(feature: String) {
        runCatching { check(feature == "anime" || feature == "manga" || feature == "novel") }
            .onSuccess {
                val list = databaseRepository.loadFeaturesFromDb(feature)
                if (list.isNotEmpty()) {
                    embed {
                        title =
                            feature.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        color = Emerald
                        list.forEach { field(it.name) { it.version } }
                    }
                }
            }
            .onFailure {
                embed {
                    title = "$feature is not a type!"
                    description = "Please choose from 'anime', 'manga', or 'novel'."
                }
            }

    }

    private suspend fun MessageCreateEvent.setDelay(messageInfo: List<String>) {
        if (member?.isOwner() == false) {
            message.channel.createMessage("Must be owner to change the delay")
            error("Only Owner can change the delay!")
        }

        val amount = messageInfo[1]
        runCatching { amount.toLong() }
            .onSuccess { settingsDb.updateSettings { delayInMillis = it } }
            .onFailure {
                if (amount == "reset")
                    settingsDb.updateSettings { delayInMillis = 60.minutes.inWholeMilliseconds }
            }

        message.channel.createMessage("Changing check delay to ${settings.first().delayInMillis}ms")
    }
}
